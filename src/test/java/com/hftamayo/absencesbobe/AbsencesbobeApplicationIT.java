package com.hftamayo.absencesbobe;

import com.hftamayo.absencesbobe.shared.test.AbstractPostgresIT;
import com.hftamayo.absencesbobe.shared.web.jackson.JacksonTrimmingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ActiveProfiles("test")
class AbsencesbobeApplicationIT extends AbstractPostgresIT {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private AbsencesbobeApplication application;

	@Test
	@DisplayName("Context starts with main application configuration")
	void contextStartsWithMainApplicationConfiguration() {
		assertThat(applicationContext).isNotNull();
		assertThat(application).isNotNull();
		assertThat(applicationContext.getBean(JacksonTrimmingConfig.class)).isNotNull();
	}

	@Test
	@DisplayName("applicationReady executes without throwing")
	void applicationReady_executesWithoutThrowing() {
		assertThatCode(application::applicationReady).doesNotThrowAnyException();
	}
}
