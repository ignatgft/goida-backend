package ru.goidaai.test_backend.service;

import java.time.Clock;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ru.goidaai.test_backend.config.AppProperties;
import ru.goidaai.test_backend.dto.AuthResponse;
import ru.goidaai.test_backend.dto.UserDTO;
import ru.goidaai.test_backend.exception.ResourceNotFoundException;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.repository.UserRepository;

/**
 * Сервис для управления JWT токенами (refresh, rotate)
 */
@Service
public class TokenRefreshService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final Clock clock;

    public TokenRefreshService(
        JwtEncoder jwtEncoder,
        JwtDecoder jwtDecoder,
        AppProperties appProperties,
        UserRepository userRepository,
        Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.appProperties = appProperties;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    /**
     * Обновить access токен используя старый (еще валидный или недавно истекший)
     * Возвращает новый access токен с обновленным сроком действия
     */
    public AuthResponse refreshToken(String oldToken) {
        try {
            // Декодируем старый токен (может быть истекшим)
            Jwt decodedToken = jwtDecoder.decode(oldToken);
            
            String userId = decodedToken.getSubject();
            String email = decodedToken.getClaim("email");
            String fullName = decodedToken.getClaim("fullName");

            // Проверяем что пользователь существует
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Создаем новый токен
            Instant issuedAt = clock.instant();
            Instant expiresAt = issuedAt.plusSeconds(appProperties.getAuth().getTokenTtlSeconds());

            JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("goida-ai")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(userId)
                .claim("email", email)
                .claim("fullName", fullName)
                .build();

            String newAccessToken = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
            ).getTokenValue();

            UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getBaseCurrency().getCode(),
                user.getMonthlyBudget()
            );

            return new AuthResponse("Bearer", newAccessToken, appProperties.getAuth().getTokenTtlSeconds(), userDTO);
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired token: " + e.getMessage(), e);
        }
    }
}
