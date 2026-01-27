package com.hftamayo.absencesbobe;

import com.sun.tools.javac.Main;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@Import(com.hftamayo.absencesbobe.shared.web.jackson.JacksonTrimmingConfig.class)
@EnableConfigurationProperties
@RequiredArgsConstructor

public class AbsencesbobeApplication {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady() {
		System.out.println("\n========================================================");
		System.out.println("        The project is up and running!");
//        System.out.println("        Version: " + com.hftamayo.java.todo.utilities.version.VersionConstants.getFullVersion());
//        System.out.println("        API Version: " + com.hftamayo.java.todo.utilities.version.VersionConstants.getCurrentApiVersion());
		System.out.println("========================================================\n");
	}

}
