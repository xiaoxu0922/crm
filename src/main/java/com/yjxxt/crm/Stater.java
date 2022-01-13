package com.yjxxt.crm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yjxxt.crm.mapper")
public class Stater {
    public static void main(String[] args) {
        SpringApplication.run(Stater.class,args);
    }
}
