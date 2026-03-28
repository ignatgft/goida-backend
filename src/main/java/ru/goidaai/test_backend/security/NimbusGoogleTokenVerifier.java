package ru.goidaai.test_backend.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.goidaai.test_backend.config.AppProperties;
import ru.goidaai.test_backend.exception.BadRequestException;
import ru.goidaai.test_backend.exception.UnauthorizedException;

@Component
public class NimbusGoogleTokenVerifier implements GoogleTokenVerifier {

    private final JwtDecoder googleIdTokenDecoder;
    private final AppProperties appProperties;

    public NimbusGoogleTokenVerifier(
        @Qualifier("googleIdTokenDecoder") JwtDecoder googleIdTokenDecoder,
        AppProperties appProperties
    ) {
        this.googleIdTokenDecoder = googleIdTokenDecoder;
        this.appProperties = appProperties;
    }

    @Override
    public GoogleUser verify(String idToken, String accessToken) {
        if (!StringUtils.hasText(appProperties.getGoogle().getWebClientId())) {
            throw new UnauthorizedException("Google sign-in is not configured on the server");
        }
        if (!StringUtils.hasText(idToken)) {
            throw new BadRequestException("Google idToken is required");
        }

        Jwt jwt = googleIdTokenDecoder.decode(idToken);
        String subject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String fullName = jwt.getClaimAsString("name");
        String pictureUrl = jwt.getClaimAsString("picture");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");

        if (!StringUtils.hasText(subject) || !StringUtils.hasText(email)) {
            throw new UnauthorizedException("Google token payload is missing required identity fields");
        }

        return new GoogleUser(
            subject,
            email.toLowerCase(),
            StringUtils.hasText(fullName) ? fullName : email,
            pictureUrl,
            Boolean.TRUE.equals(emailVerified)
        );
    }
}
