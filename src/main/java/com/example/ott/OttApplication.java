package com.example.ott;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching // 캐싱 기능 활성화
@EnableJpaAuditing
@SpringBootApplication
@EnableScheduling // 스케쥴링을 위한 어노테이션
@EnableAsync
public class OttApplication {
	public static void main(String[] args) {
		SpringApplication.run(OttApplication.class, args);
	}
}