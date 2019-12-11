package com.spotonresponse.saber;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class WebserviceApplication {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {

        SpringApplication.run(WebserviceApplication.class, args);
    }

}

