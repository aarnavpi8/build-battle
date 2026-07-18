package com.substring.buildbattle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BuildBattleApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuildBattleApplication.class, args);
	}

}
