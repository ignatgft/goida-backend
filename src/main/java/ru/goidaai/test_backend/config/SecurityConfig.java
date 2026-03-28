package ru.goidaai.test_backend.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Конфигурация безопасности
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder appJwtDecoder) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Публичные эндпоинты
                .requestMatchers("/error", "/uploads/**", "/h2-console/**").permitAll()
                .requestMatchers("/api/auth/google", "/api/auth/dev").permitAll()
                // Публичные API для курсов валют
                .requestMatchers("/api/rates/**").permitAll()
                // Все остальные требуют аутентификации
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(appJwtDecoder)))
            // Для H2 консоли
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(AppProperties appProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        
        List<String> allowedOrigins = Arrays.stream(appProperties.getSecurity().getAllowedOrigins().split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();

        // Добавляем Android эмулятор
        if (!allowedOrigins.contains("http://10.0.2.2:8080")) {
            allowedOrigins = new java.util.ArrayList<>(allowedOrigins);
            allowedOrigins.add("http://10.0.2.2:8080");
        }

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecretKey appJwtSecretKey(AppProperties appProperties) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(appProperties.getAuth().getSecret().getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(digest, "HmacSHA256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Unable to initialize JWT secret key", exception);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey appJwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(appJwtSecretKey));
    }

    @Bean
    public JwtDecoder appJwtDecoder(SecretKey appJwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(appJwtSecretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }

    @Bean
    public JwtDecoder googleIdTokenDecoder(AppProperties appProperties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(appProperties.getGoogle().getJwkSetUri()).build();
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            googleIssuerValidator(),
            googleAudienceValidator(appProperties.getGoogle().getWebClientId())
        );
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }

    private OAuth2TokenValidator<Jwt> googleIssuerValidator() {
        return jwt -> {
            String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : jwt.getClaimAsString("iss");
            boolean valid = "https://accounts.google.com".equals(issuer)
                || "accounts.google.com".equals(issuer);

            return valid
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "Google token issuer is invalid",
                    null
                ));
        };
    }

    private OAuth2TokenValidator<Jwt> googleAudienceValidator(String googleWebClientId) {
        return jwt -> {
            boolean valid = googleWebClientId != null
                && !googleWebClientId.isBlank()
                && jwt.getAudience() != null
                && jwt.getAudience().contains(googleWebClientId);

            return valid
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "Google token audience does not match configured web client id",
                    null
                ));
        };
    }
}
