package com.example.ott.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 게임 이미지
        registry.addResourceHandler("/images/gameimages/**")
                .addResourceLocations("file:///C:/upload/images/gameimages/")
                .setCachePeriod(3600);

        // 영화 이미지
        registry.addResourceHandler("/images/movieimages/**")
                .addResourceLocations("file:///C:/upload/images/movieimages/")
                .setCachePeriod(3600);

        // 업로드 파일
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);
    }

    /**
     * 정적 리소스 요청이 컨트롤러 매핑보다 우선하도록 설정
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 기본 static 리소스 핸들러를 우선 적용
        configurer.setUseRegisteredSuffixPatternMatch(true);
    }
}
