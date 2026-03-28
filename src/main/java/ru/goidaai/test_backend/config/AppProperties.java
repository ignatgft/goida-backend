package ru.goidaai.test_backend.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Auth auth = new Auth();
    private final Security security = new Security();
    private final Google google = new Google();
    private final Ai ai = new Ai();
    private final Rates rates = new Rates();
    private final Storage storage = new Storage();
    private final Receipt receipt = new Receipt();

    public Auth getAuth() {
        return auth;
    }

    public Security getSecurity() {
        return security;
    }

    public Google getGoogle() {
        return google;
    }

    public Ai getAi() {
        return ai;
    }

    public Rates getRates() {
        return rates;
    }

    public Storage getStorage() {
        return storage;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public static class Auth {

        @NotBlank
        private String secret = "change-me";

        private long tokenTtlSeconds = 86_400;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getTokenTtlSeconds() {
            return tokenTtlSeconds;
        }

        public void setTokenTtlSeconds(long tokenTtlSeconds) {
            this.tokenTtlSeconds = tokenTtlSeconds;
        }
    }

    public static class Security {

        @NotBlank
        private String allowedOrigins = "http://localhost:3000,http://localhost:5173,http://localhost:8080";

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Google {

        private String webClientId = "";
        private String jwkSetUri = "https://www.googleapis.com/oauth2/v3/certs";

        public String getWebClientId() {
            return webClientId;
        }

        public void setWebClientId(String webClientId) {
            this.webClientId = webClientId;
        }

        public String getJwkSetUri() {
            return jwkSetUri;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
        }
    }

    public static class Ai {

        private String provider = "mock";
        private String model = "llama-3.3-70b-versatile";
        private String baseUrl = "https://api.groq.com/openai/v1";
        private String openaiKey = "";
        private String groqApiKey = "";
        private String groqBaseUrl = "https://api.groq.com/openai/v1";
        private String groqModel = "llama-3.3-70b-versatile";

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getOpenaiKey() {
            return openaiKey;
        }

        public void setOpenaiKey(String openaiKey) {
            this.openaiKey = openaiKey;
        }

        public String getGroqApiKey() {
            return groqApiKey;
        }

        public void setGroqApiKey(String groqApiKey) {
            this.groqApiKey = groqApiKey;
        }

        public String getGroqBaseUrl() {
            return groqBaseUrl;
        }

        public void setGroqBaseUrl(String groqBaseUrl) {
            this.groqBaseUrl = groqBaseUrl;
        }

        public String getGroqModel() {
            return groqModel;
        }

        public void setGroqModel(String groqModel) {
            this.groqModel = groqModel;
        }
    }

    public static class Rates {

        private String provider = "mock";
        private String apiKey = "";

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class Storage {

        private String uploadDir = "./storage";
        private String publicBaseUrl = "";

        public String getUploadDir() {
            return uploadDir;
        }

        public void setUploadDir(String uploadDir) {
            this.uploadDir = uploadDir;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
        }
    }

    public static class Receipt {

        private String provider = "mock";

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }
    }
}
