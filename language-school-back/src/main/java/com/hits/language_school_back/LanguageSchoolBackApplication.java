package com.hits.language_school_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LanguageSchoolBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(LanguageSchoolBackApplication.class, args);
	}

}
