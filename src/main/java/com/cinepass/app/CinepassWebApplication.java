package com.cinepass.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CinepassWebApplication {
	public static void main(String[] args) {
		SpringApplication.run(CinepassWebApplication.class, args);
	}
}
