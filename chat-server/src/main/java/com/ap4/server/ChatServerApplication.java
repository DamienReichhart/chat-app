package com.ap4.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatServerApplication {
	private static Logger logger = LogManager.getLogger(ChatServerApplication.class);
	public static void main(String[] args) {
		logger.info("Starting ChatServerApplication");
		SpringApplication.run(ChatServerApplication.class, args);
	}
}
