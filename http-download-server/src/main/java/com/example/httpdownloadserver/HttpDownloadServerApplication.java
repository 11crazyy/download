package com.example.httpdownloadserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.example.httpdownloadserver.dao")
public class HttpDownloadServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HttpDownloadServerApplication.class, args);
    }

}
