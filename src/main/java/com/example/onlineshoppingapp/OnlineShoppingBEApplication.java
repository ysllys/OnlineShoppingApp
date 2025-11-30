package com.example.onlineshoppingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class OnlineShoppingBEApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineShoppingBEApplication.class, args);
    }

}
