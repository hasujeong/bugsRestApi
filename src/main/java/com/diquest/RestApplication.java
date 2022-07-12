package com.diquest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication
public class RestApplication extends SpringBootServletInitializer {

	@Override 
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(RestApplication.class);
    }
	
	public static void main(String[] args) {
//		SpringApplication.run(RestApplication.class, args);
		new SpringApplicationBuilder(RestApplication.class).build().run(args);
	}

}
