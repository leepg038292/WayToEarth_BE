package com.waytoearth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@EnableRedisRepositories
public class WaytoearthBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(WaytoearthBackendApplication.class, args);



	}

}
