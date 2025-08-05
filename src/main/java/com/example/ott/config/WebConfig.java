package com.example.ott.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//업로드 파일을 위한 리소스 핸들러 설정
// uploads/ 경로로 접근 시 실제 파일 시스템의 uploads/ 폴더를 참조하도록 설정
// 이 설정은 파일 업로드 기능을 구현할 때 유용합니다.

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        registry.addResourceHandler("/images/gameimages/**")
                .addResourceLocations("file:///C:/upload/images/gameimages/");

        registry.addResourceHandler("/images/movieimages/**")
                .addResourceLocations("file:///C:/upload/images/movieimages/");
    }

}
