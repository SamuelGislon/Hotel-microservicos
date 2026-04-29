package com.hosped.ms_pagamentos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MsPagamentosApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsPagamentosApplication.class, args);
    }
}
