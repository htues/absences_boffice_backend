# Stage 1: Build the application
# UPDATED: Changed to Java 21 to match project requirements
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace/app

# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# ADDED: Ensure mvnw is executable (fixes common permission issues on Linux/CI)
RUN chmod +x mvnw

# Clear any corrupted cache and download dependencies
RUN rm -rf /root/.m2/repository/net/bytebuddy 2>/dev/null || true
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src src

# Build the application with production profile
# RUN ./mvnw -B -DskipTests clean package
RUN ./mvnw -B -Dskip.tests=true -Dskip.integration.tests=true -Dskip.coverage.check=true clean package

# Stage 2: Extract layered JAR for faster startup
# UPDATED: Changed to Java 21
FROM eclipse-temurin:21-jdk-alpine AS extract
WORKDIR /workspace/app

# Copy ONLY the Spring Boot repackaged jar (avoid *.jar.original)
COPY --from=build /workspace/app/target/*-SNAPSHOT.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Run the application
# UPDATED: Changed to Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# setup the timezone
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/America/Chicago /etc/localtime && \
    echo "America/Chicago" > /etc/timezone && \
    apk del tzdata

# Add non-root user for security
RUN addgroup --system --gid 1001 appgroup \
    && adduser --system --uid 1001 --ingroup appgroup appuser \
    && mkdir -p /logs /resources \
    && chown -R appuser:appgroup /app /logs /resources

# Copy layered application
COPY --from=extract --chown=appuser:appgroup /workspace/app/dependencies/ ./
COPY --from=extract --chown=appuser:appgroup /workspace/app/spring-boot-loader/ ./
COPY --from=extract --chown=appuser:appgroup /workspace/app/snapshot-dependencies/ ./
COPY --from=extract --chown=appuser:appgroup /workspace/app/application/ ./

# Crear volúmenes para logs y configuración
VOLUME ["/logs", "/resources"]

# Habilitar health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health/readiness || exit 1

EXPOSE 8080

# Cambiar al usuario no-root
USER appuser

# Configurar opciones JVM optimizadas para contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+OptimizeStringConcat -XX:+UseStringDeduplication"

# Ejecutar la aplicación
# UPDATED: Added 'exec' and used the correct Launcher for Spring Boot 3.2+ (which includes 4.0 snapshots)
# Ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]

#how to run this file:
#multiplatform image
#docker buildx build --no-cache --platform linux/amd64,linux/arm64 -t myimage:latest .
#docker buildx build --no-cache --platform linux/amd64,linux/arm64 -t hftamayo/absencesbobe:0.0.1 -f Dockerfile.app .

#specific platform
#docker buildx build --no-cache --platform linux/amd64 -t hftamayo/absencesbobe:0.0.1 -f Dockerfile.app .
#docker build --no-cache -t hftamayo/absencesbobe:0.0.1 -f Dockerfile.app .

#docker run -d --name absencesbobe --network developer_network -p 8081:8080 --env-file .env hftamayo/absencesbobe:0.0.1
