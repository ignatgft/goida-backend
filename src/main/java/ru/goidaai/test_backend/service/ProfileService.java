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
        return new AvatarUploadResponse(avatarUrl);
    }
}
