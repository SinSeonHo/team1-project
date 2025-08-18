package com.example.ott.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/images/movieimages/**")
                                .addResourceLocations(
                                                "file:///C:/upload/images/movieimages/", // 외부
                                                "classpath:/static/images/movieimages/" // 클래스패스 fallback
                                )
                                .setCachePeriod(3600);

                registry.addResourceHandler("/images/gameimages/**")
                                .addResourceLocations(
                                                "file:///C:/upload/images/gameimages/",
                                                "classpath:/static/images/gameimages/")
                                .setCachePeriod(3600);

                registry.addResourceHandler("/uploads/**") // ← 복수로 통일
                                .addResourceLocations("file:///C:/upload/") // ← 절대 경로
                                .setCachePeriod(3600);
        }
        // configurePathMatch(...)는 삭제 (혼선만 줌)

}
