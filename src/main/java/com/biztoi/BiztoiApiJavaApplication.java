package com.biztoi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class BiztoiApiJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BiztoiApiJavaApplication.class, args);
	}

	@GetMapping("/")
	public String getTest() {
		return "hello world!!";
	}
}
