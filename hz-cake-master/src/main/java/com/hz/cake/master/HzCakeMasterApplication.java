package com.hz.cake.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class HzCakeMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(HzCakeMasterApplication.class, args);
    }

}
