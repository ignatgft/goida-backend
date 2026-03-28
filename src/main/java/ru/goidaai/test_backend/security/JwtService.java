package ru.goidaai.test_backend.security;

import java.time.Clock;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import ru.goidaai.test_backend.config.AppProperties;
import ru.goidaai.test_backend.dto.AuthResponse;
import ru.goidaai.test_backend.dto.UserDTO;
import ru.goidaai.test_backend.model.User;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final AppProperties appProperties;
    private final Clock clock;

    public JwtService(JwtEncoder jwtEncoder, AppProperties appProperties, Clock clock) {
        this.jwtEncoder = jwtEncoder;
        this.appProperties = appProperties;
        this.clock = clock;
    }

    public AuthResponse buildAuthResponse(User user, UserDTO userDTO) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plusSeconds(appProperties.getAuth().getTokenTtlSeconds());

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("goida-ai")
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .subject(user.getId())
            .claim("email", user.getEmail())
            .claim("fullName", user.getFullName())
            .build();

        String accessToken = jwtEncoder.encode(
            JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        ).getTokenValue();

        return new AuthResponse("Bearer", accessToken, appProperties.getAuth().getTokenTtlSeconds(), userDTO);
    }
}
