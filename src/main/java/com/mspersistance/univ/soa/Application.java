package com.mspersistance.univ.soa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.out.println("\n" + "âœ… Microservice Persistance dÃ©marrÃ©!\n");
	}

}
