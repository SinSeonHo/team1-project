package com.example.ott.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// (선택) 캐시 제어를 더 엄격히 하고 싶다면 아래 주석 해제
// import org.springframework.http.CacheControl;
// import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {

                // 공통: /images/** -> /var/lib/ott/static/images/
                registry.addResourceHandler("/images/**")
                                .addResourceLocations(
                                                "file:/var/lib/ott/static/images/", // 외부(운영)
                                                "classpath:/static/images/" // 클래스패스 fallback
                                )
                                .setCachePeriod(3600);
                // .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic()); // 선택

                // 필요하면 세부 path도 별칭으로 유지 가능(선택):
                registry.addResourceHandler("/images/movieimages/**")
                                .addResourceLocations(
                                                "file:/var/lib/ott/static/images/movieimages/",
                                                "classpath:/static/images/movieimages/")
                                .setCachePeriod(3600);

                registry.addResourceHandler("/images/gameimages/**")
                                .addResourceLocations(
                                                "file:/var/lib/ott/static/images/gameimages/",
                                                "classpath:/static/images/gameimages/")
                                .setCachePeriod(3600);

                // 예전 /uploads/** 매핑은 리눅스 절대 경로로 교체(쓰는 중일 때만)
                registry.addResourceHandler("/uploads/**")
                                .addResourceLocations("file:/var/lib/ott/") // 필요 없으면 이 블록 삭제
                                .setCachePeriod(3600);
        }
}
