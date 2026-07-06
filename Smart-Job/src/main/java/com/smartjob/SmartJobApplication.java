package com.smartjob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartJobApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartJobApplication.class, args);
	}

}
