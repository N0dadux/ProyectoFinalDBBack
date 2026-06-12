package com.cocobongo.cerveceria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync   // Habilita el @Async en AuditService
@EnableScheduling  // Habilita el @Scheduled 
public class CerveceriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CerveceriaApplication.class, args);
	}

}
