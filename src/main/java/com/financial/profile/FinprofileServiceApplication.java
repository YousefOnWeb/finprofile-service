package com.financial.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application.
 *
 * <p>Think of this class as the restaurant manager: it opens the doors,
 * hires the staff (Spring beans), and starts listening for customer orders
 * (HTTP requests).</p>
 *
 * <ul>
 *   <li>{@code @SpringBootApplication} tells Spring to scan this package
 *       ({@code com.financial.profile}) and all sub-packages for components
 *      such as {@code @RestController}, {@code @Service}, and {@code @Repository}.</li>
 *   <li>{@code main(...)} launches the embedded Tomcat server and the Spring
 *      application context.</li>
 * </ul>
 */
@SpringBootApplication
public class FinprofileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinprofileServiceApplication.class, args);
    }
}
