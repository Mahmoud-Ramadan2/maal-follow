package com.mahmoud.maalflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Mahmoud
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MaalFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaalFlowApplication.class, args);
    }

}
