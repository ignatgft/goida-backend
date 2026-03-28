package ru.goidaai.test_backend.service;

import java.math.BigDecimal;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.goidaai.test_backend.dto.AuthResponse;
import ru.goidaai.test_backend.dto.DevLoginRequest;
import ru.goidaai.test_backend.dto.GoogleAuthRequest;
import ru.goidaai.test_backend.dto.UserDTO;
import ru.goidaai.test_backend.exception.BadRequestException;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.model.enums.AuthProvider;
import ru.goidaai.test_backend.repository.UserRepository;
import ru.goidaai.test_backend.security.GoogleTokenVerifier;
import ru.goidaai.test_backend.security.GoogleUser;
import ru.goidaai.test_backend.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final JwtService jwtService;
    private final DtoFactory dtoFactory;
    private final Clock clock;

    public AuthService(
        UserRepository userRepository,
        GoogleTokenVerifier googleTokenVerifier,
        JwtService jwtService,
        DtoFactory dtoFactory,
        Clock clock
    ) {
        this.userRepository = userRepository;
        this.googleTokenVerifier = googleTokenVerifier;
        this.jwtService = jwtService;
        this.dtoFactory = dtoFactory;
        this.clock = clock;
    }

    @Transactional
    public AuthResponse googleLogin(GoogleAuthRequest request) {
        GoogleUser googleUser = resolveGoogleUser(request);

        User user = userRepository.findByGoogleSubject(googleUser.subject())
            .or(() -> userRepository.findByEmailIgnoreCase(googleUser.email()))
            .orElseGet(User::new);

        user.setEmail(googleUser.email());
        user.setFullName(googleUser.fullName());
        user.setAvatarUrl(googleUser.pictureUrl());
        user.setBaseCurrency(user.getBaseCurrency() == null ? "USD" : user.getBaseCurrency());
        user.setMonthlyBudget(user.getMonthlyBudget() == null ? BigDecimal.ZERO : user.getMonthlyBudget());
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setGoogleSubject(googleUser.subject());
        user.setEmailVerified(googleUser.emailVerified());
        user.setLastLoginAt(clock.instant());

        User savedUser = userRepository.save(user);
        UserDTO userDTO = dtoFactory.toUserDto(savedUser);
        return jwtService.buildAuthResponse(savedUser, userDTO);
    }

    @Transactional
    public AuthResponse devLogin(DevLoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseGet(User::new);

        user.setEmail(request.email().trim().toLowerCase());
        user.setFullName(StringUtils.hasText(request.fullName()) ? request.fullName() : request.email());
        user.setAvatarUrl(request.avatarUrl());
        user.setBaseCurrency(user.getBaseCurrency() == null ? "USD" : user.getBaseCurrency());
        user.setMonthlyBudget(user.getMonthlyBudget() == null ? BigDecimal.ZERO : user.getMonthlyBudget());
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setGoogleSubject("dev-" + user.getEmail());
        user.setEmailVerified(true);
        user.setLastLoginAt(clock.instant());

        User savedUser = userRepository.save(user);
        UserDTO userDTO = dtoFactory.toUserDto(savedUser);
        return jwtService.buildAuthResponse(savedUser, userDTO);
    }

    private GoogleUser resolveGoogleUser(GoogleAuthRequest request) {
        if (StringUtils.hasText(request.idToken())) {
            return googleTokenVerifier.verify(request.idToken(), request.accessToken());
        }

        if (!StringUtils.hasText(request.email())) {
            throw new BadRequestException("Google email is required when idToken is missing");
        }

        String email = request.email().trim().toLowerCase();
        String subject = StringUtils.hasText(request.googleSubject())
            ? request.googleSubject().trim()
            : email;
        String fullName = StringUtils.hasText(request.displayName())
            ? request.displayName().trim()
            : email;
        String photoUrl = StringUtils.hasText(request.photoUrl())
            ? request.photoUrl().trim()
            : null;

        return new GoogleUser(subject, email, fullName, photoUrl, true);
    }
}
