package com.bptn.feedapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FeedAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedAppApplication.class, args);
	}

}
