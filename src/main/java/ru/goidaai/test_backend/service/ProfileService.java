package ru.goidaai.test_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.dto.AvatarUploadResponse;
import ru.goidaai.test_backend.dto.UserDTO;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.repository.UserRepository;

@Service
public class ProfileService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final DtoFactory dtoFactory;

    public ProfileService(
        CurrentUserService currentUserService,
        UserRepository userRepository,
        StorageService storageService,
        DtoFactory dtoFactory
    ) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.dtoFactory = dtoFactory;
    }

    @Transactional(readOnly = true)
    public UserDTO getProfile(String userId) {
        return dtoFactory.toUserDto(currentUserService.require(userId));
    }

    @Transactional
    public AvatarUploadResponse uploadAvatar(String userId, MultipartFile file) {
        User user = currentUserService.require(userId);
        String avatarUrl = storageService.storeImage(file, "avatars");
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        
        AvatarUploadResponse response = new AvatarUploadResponse();
        response.setUrl(avatarUrl);
        response.setFileName(file.getOriginalFilename());
        response.setContentType(file.getContentType());
        response.setFileSize(file.getSize());
        return response;
    }

    @Transactional
    public void deleteAvatar(String userId) {
        User user = currentUserService.require(userId);
        if (user.getAvatarUrl() != null) {
            storageService.deleteFile(user.getAvatarUrl());
            user.setAvatarUrl(null);
            userRepository.save(user);
        }
    }

    @Transactional
    public UserDTO updateProfile(
            String userId,
            String name,
            String baseCurrency,
            Double monthlyBudget,
            String language,
            String theme,
            Boolean emailNotifications,
            Boolean pushNotifications,
            String timezone) {
        User user = currentUserService.require(userId);

        if (name != null) user.setName(name);
        if (baseCurrency != null) user.setBaseCurrency(baseCurrency);
        if (monthlyBudget != null) user.setMonthlyBudget(
            java.math.BigDecimal.valueOf(monthlyBudget));
        if (language != null) user.setLanguage(language);
        if (theme != null) user.setTheme(theme);
        if (emailNotifications != null) user.setEmailNotifications(emailNotifications);
        if (pushNotifications != null) user.setPushNotifications(pushNotifications);
        if (timezone != null) user.setTimezone(timezone);

        userRepository.save(user);
        return dtoFactory.toUserDto(user);
    }
}
