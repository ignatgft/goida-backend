package ru.goidaai.test_backend.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public WebMvcConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadRoot = Path.of(appProperties.getStorage().getUploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadRoot.resolve("avatars"));
            Files.createDirectories(uploadRoot.resolve("receipts"));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize storage directories", exception);
        }

        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(uploadRoot.toUri().toString());
    }
}
