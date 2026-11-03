package com.shopsmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the ShopSmart Kenya application.
 *
 * @SpringBootApplication is a shortcut for three annotations:
 *   @Configuration            - this class can define Spring beans
 *   @EnableAutoConfiguration  - Spring Boot configures Tomcat, JPA, Security, Thymeleaf for us
 *   @ComponentScan            - scan com.shopsmart.* for @Controller, @Service, @Repository classes
 *
 * Running main() starts the embedded Tomcat server on http://localhost:8080
 * so you never need to install a separate Tomcat like in old NetBeans web projects.
 */
@SpringBootApplication
public class ShopSmartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopSmartApplication.class, args);
    }
}
