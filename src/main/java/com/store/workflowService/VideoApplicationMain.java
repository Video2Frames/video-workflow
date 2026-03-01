package com.store.workflowService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VideoApplicationMain {

    public static void main(String[] args) {
        SpringApplication.run(VideoApplicationMain.class, args);
    }

}
