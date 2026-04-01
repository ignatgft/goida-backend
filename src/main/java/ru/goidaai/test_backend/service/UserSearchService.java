package ru.goidaai.test_backend.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.dto.UserSearchResultDTO;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.repository.UserRepository;

@Service
public class UserSearchService {

    private final UserRepository userRepository;

    public UserSearchService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserSearchResultDTO> searchUsers(String query) {
        if (query == null || query.trim().isEmpty() || query.length() < 2) {
            return List.of();
        }

        String cleanQuery = query.trim().replace("@", "");
        List<User> users = userRepository.searchByUsernameOrName(cleanQuery);

        return users.stream()
                .limit(20)
                .map(this::mapToSearchResultDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserSearchResultDTO getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::mapToSearchResultDTO)
                .orElse(null);
    }

    private UserSearchResultDTO mapToSearchResultDTO(User user) {
        UserSearchResultDTO dto = new UserSearchResultDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
