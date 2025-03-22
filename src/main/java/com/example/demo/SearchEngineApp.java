package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@SpringBootApplication
public class SearchEngineApp implements CommandLineRunner {

    @Autowired
    private FileCrawler fileCrawler;

    public static void main(String[] args) {
        SpringApplication.run(SearchEngineApp.class, args);
    }

    @Override
    public void run(String... args) throws IOException {
        fileCrawler.startWatching();
        System.out.println("File crawling completed!");
    }
}

