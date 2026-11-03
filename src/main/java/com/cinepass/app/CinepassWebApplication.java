package com.cinepass.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CinepassWebApplication {
	public static void main(String[] args) {
		SpringApplication.run(CinepassWebApplication.class, args);
	}
}