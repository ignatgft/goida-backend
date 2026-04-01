package ru.goidaai.test_backend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.config.AppProperties;
import ru.goidaai.test_backend.dto.AvatarInfoDTO;
import ru.goidaai.test_backend.dto.AvatarUploadResponse;
import ru.goidaai.test_backend.exception.BadRequestException;
import ru.goidaai.test_backend.exception.ResourceNotFoundException;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.model.UserAvatar;
import ru.goidaai.test_backend.repository.UserAvatarRepository;

@Service
public class AvatarService {

    private final UserAvatarRepository avatarRepository;
    private final StorageService storageService;
    private final CurrentUserService currentUserService;
    private final String uploadDir;

    public AvatarService(
            UserAvatarRepository avatarRepository,
            StorageService storageService,
            CurrentUserService currentUserService,
            AppProperties appProperties) {
        this.avatarRepository = avatarRepository;
        this.storageService = storageService;
        this.currentUserService = currentUserService;
        this.uploadDir = appProperties.getStorage().getUploadDir();
    }

    @Transactional
    public AvatarUploadResponse uploadAvatar(MultipartFile file) {
        validateImageFile(file);
        
        User user = currentUserService.getCurrentUser();
        
        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new BadRequestException("Invalid image file");
            }
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            String fileName = UUID.randomUUID() + "." + getFileExtension(file.getOriginalFilename());
            Path avatarDir = Path.of(uploadDir, "avatars").toAbsolutePath().normalize();
            Files.createDirectories(avatarDir);
            Path filePath = avatarDir.resolve(fileName);
            
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = buildPublicUrl("avatars", fileName);
            
            UserAvatar avatar = new UserAvatar();
            avatar.setUser(user);
            avatar.setFileName(fileName);
            avatar.setFilePath(filePath.toString());
            avatar.setFileUrl(fileUrl);
            avatar.setContentType(file.getContentType());
            avatar.setFileSize(file.getSize());
            avatar.setWidth(originalWidth);
            avatar.setHeight(originalHeight);
            avatar.setIsActive(true);
            avatar.setUploadedAt(Instant.now());
            
            avatarRepository.deactivateAllByUser(user);
            UserAvatar savedAvatar = avatarRepository.save(avatar);
            
            user.setAvatarUrl(fileUrl);
            
            return mapToResponse(savedAvatar);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to process avatar image", e);
        }
    }

    @Transactional
    public AvatarUploadResponse resizeAvatar(String avatarId, Integer targetWidth, Integer targetHeight, boolean maintainAspectRatio) {
        User user = currentUserService.getCurrentUser();
        
        UserAvatar avatar = avatarRepository.findById(avatarId)
                .orElseThrow(() -> new ResourceNotFoundException("Avatar not found with id: " + avatarId));
        
        if (!avatar.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Avatar does not belong to current user");
        }
        
        try {
            Path avatarPath = Path.of(avatar.getFilePath());
            BufferedImage originalImage = ImageIO.read(avatarPath.toFile());
            
            if (originalImage == null) {
                throw new BadRequestException("Cannot read avatar image file");
            }
            
            int newWidth, newHeight;
            if (maintainAspectRatio) {
                double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
                if (targetWidth != null && targetHeight == null) {
                    newWidth = targetWidth;
                    newHeight = (int) (targetWidth / aspectRatio);
                } else if (targetHeight != null && targetWidth == null) {
                    newHeight = targetHeight;
                    newWidth = (int) (targetHeight * aspectRatio);
                } else if (targetWidth != null && targetHeight != null) {
                    double targetRatio = (double) targetWidth / targetHeight;
                    if (targetRatio > aspectRatio) {
                        newHeight = targetHeight;
                        newWidth = (int) (targetHeight * aspectRatio);
                    } else {
                        newWidth = targetWidth;
                        newHeight = (int) (targetWidth / aspectRatio);
                    }
                } else {
                    newWidth = originalImage.getWidth();
                    newHeight = originalImage.getHeight();
                }
            } else {
                newWidth = targetWidth != null ? targetWidth : originalImage.getWidth();
                newHeight = targetHeight != null ? targetHeight : originalImage.getHeight();
            }
            
            BufferedImage resizedImage = Scalr.resize(
                    originalImage,
                    Scalr.Method.AUTOMATIC,
                    Scalr.Mode.AUTOMATIC,
                    newWidth,
                    newHeight,
                    Scalr.OP_ANTIALIAS
            );
            
            String newFileName = UUID.randomUUID() + "." + getFileExtension(avatar.getFileName());
            Path avatarDir = Path.of(uploadDir, "avatars").toAbsolutePath().normalize();
            Files.createDirectories(avatarDir);
            Path newFilePath = avatarDir.resolve(newFileName);
            
            String formatName = avatar.getFileName().contains(".") 
                    ? avatar.getFileName().substring(avatar.getFileName().lastIndexOf('.') + 1) 
                    : "png";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, formatName, baos);
            byte[] resizedBytes = baos.toByteArray();
            
            try (InputStream is = new ByteArrayInputStream(resizedBytes)) {
                Files.copy(is, newFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            
            String newFileUrl = buildPublicUrl("avatars", newFileName);
            
            UserAvatar newAvatar = new UserAvatar();
            newAvatar.setUser(user);
            newAvatar.setFileName(newFileName);
            newAvatar.setFilePath(newFilePath.toString());
            newAvatar.setFileUrl(newFileUrl);
            newAvatar.setContentType(avatar.getContentType());
            newAvatar.setFileSize((long) resizedBytes.length);
            newAvatar.setWidth(newWidth);
            newAvatar.setHeight(newHeight);
            newAvatar.setIsActive(true);
            newAvatar.setUploadedAt(Instant.now());
            
            avatarRepository.deactivateAllByUser(user);
            UserAvatar savedAvatar = avatarRepository.save(newAvatar);
            
            user.setAvatarUrl(newFileUrl);
            
            return mapToResponse(savedAvatar);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to resize avatar image", e);
        }
    }

    @Transactional(readOnly = true)
    public List<AvatarInfoDTO> getUserAvatars(String userId) {
        List<UserAvatar> avatars = avatarRepository.findByUserIdOrderByUploadedAtDesc(userId);
        return avatars.stream()
                .map(this::mapToInfoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<AvatarInfoDTO> getActiveAvatar(String userId) {
        return avatarRepository.findByUserIdAndIsActive(userId, true)
                .map(this::mapToInfoDTO);
    }

    @Transactional
    public void deleteAvatar(String avatarId) {
        User user = currentUserService.getCurrentUser();
        
        UserAvatar avatar = avatarRepository.findById(avatarId)
                .orElseThrow(() -> new ResourceNotFoundException("Avatar not found with id: " + avatarId));
        
        if (!avatar.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Avatar does not belong to current user");
        }
        
        try {
            Path avatarPath = Path.of(avatar.getFilePath());
            if (Files.exists(avatarPath)) {
                Files.delete(avatarPath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete avatar file", e);
        }
        
        avatarRepository.delete(avatar);
    }

    @Transactional
    public void setActiveAvatar(String avatarId) {
        User user = currentUserService.getCurrentUser();
        
        UserAvatar avatar = avatarRepository.findById(avatarId)
                .orElseThrow(() -> new ResourceNotFoundException("Avatar not found with id: " + avatarId));
        
        if (!avatar.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Avatar does not belong to current user");
        }
        
        avatarRepository.deactivateAllByUser(user);
        avatarRepository.activateByIdAndUser(avatarId, user);
        user.setAvatarUrl(avatar.getFileUrl());
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed: JPG, JPEG, PNG, WEBP");
        }
        
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new BadRequestException("File size must not exceed 10MB");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "png";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String buildPublicUrl(String folder, String filename) {
        return "/uploads/" + folder + "/" + filename;
    }

    private AvatarUploadResponse mapToResponse(UserAvatar avatar) {
        AvatarUploadResponse response = new AvatarUploadResponse();
        response.setId(avatar.getId());
        response.setUrl(avatar.getFileUrl());
        response.setFileName(avatar.getFileName());
        response.setContentType(avatar.getContentType());
        response.setFileSize(avatar.getFileSize());
        response.setWidth(avatar.getWidth());
        response.setHeight(avatar.getHeight());
        response.setUploadTimestamp(java.math.BigDecimal.valueOf(avatar.getUploadedAt().toEpochMilli()));
        return response;
    }

    private AvatarInfoDTO mapToInfoDTO(UserAvatar avatar) {
        return new AvatarInfoDTO(
                avatar.getId(),
                avatar.getFileUrl(),
                avatar.getFileName(),
                avatar.getContentType(),
                avatar.getFileSize(),
                avatar.getWidth(),
                avatar.getHeight(),
                avatar.getIsActive(),
                java.math.BigDecimal.valueOf(avatar.getUploadedAt().toEpochMilli())
        );
    }
}
