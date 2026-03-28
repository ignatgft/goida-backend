package ru.goidaai.test_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "ru.goidaai.test_backend")
public class TestBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestBackendApplication.class, args);
	}

}
