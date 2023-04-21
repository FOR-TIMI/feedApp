package com.bptn.feedapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@EnableAsync
@SpringBootApplication
@PropertySources({
    @PropertySource("classpath:application.yml"),
    @PropertySource(value = "file:${user.dir}/.env", ignoreResourceNotFound = true)
})
public class FeedAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedAppApplication.class, args);
	}

}
