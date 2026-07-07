package com.lucabridge.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();
    private final I18n i18n = new I18n();
    private final Storage storage = new Storage();

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMinutes;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
    }

    @Data
    public static class I18n {
        private String defaultLang;
        private List<String> supportedLangs;
    }

    @Data
    public static class Storage {
        private String endpoint;
        private String publicBaseUrl;
        private String bucket;
        private String accessKey;
        private String secretKey;
        private String region;
    }
}
