package com.example.ptmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PtManagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(PtManagerApplication.class, args);
	}
}
