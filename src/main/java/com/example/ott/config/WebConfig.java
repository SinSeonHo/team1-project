package com.example.ott.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
