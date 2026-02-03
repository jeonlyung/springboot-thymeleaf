package com.project.springboot_thymeleaf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}) // DB 설정 제외
@SpringBootApplication
public class SpringbootThymeleafApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootThymeleafApplication.class, args);
	}

}
