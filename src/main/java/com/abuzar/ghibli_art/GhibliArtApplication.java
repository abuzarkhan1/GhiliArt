package com.abuzar.ghibli_art;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GhibliArtApplication {

    public static void main(String[] args) {

        SpringApplication.run(GhibliArtApplication.class, args);
    }

}
