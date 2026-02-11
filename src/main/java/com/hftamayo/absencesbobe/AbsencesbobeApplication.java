package com.hftamayo.absencesbobe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@Import(com.hftamayo.absencesbobe.shared.web.jackson.JacksonTrimmingConfig.class)
@EnableConfigurationProperties
@RequiredArgsConstructor
@Slf4j

public class AbsencesbobeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AbsencesbobeApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady() {
		log.info("        The project is up and running!");
//        System.out.println("        Version: " + com.hftamayo.java.todo.utilities.version.VersionConstants.getFullVersion());
//        System.out.println("        API Version: " + com.hftamayo.java.todo.utilities.version.VersionConstants.getCurrentApiVersion());
	}

}
