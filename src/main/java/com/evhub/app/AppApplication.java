package com.evhub.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
// (exclude = { DataSourceAutoConfiguration.class })
(exclude = {
		UserDetailsServiceAutoConfiguration.class,
		DataSourceAutoConfiguration.class,
}, scanBasePackages = { "com.**" })
public class AppApplication {
	public static final ExecutorService executor = Executors.newFixedThreadPool(10);

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
		System.out.println("Hello prabir");
	}

}
