package com.waytoearth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class WaytoearthBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(WaytoearthBackendApplication.class, args);
		System.out.println(">>> DB_URL from env = " + System.getenv("DB_URL"));



	}

}
