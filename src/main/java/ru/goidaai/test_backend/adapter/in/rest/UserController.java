package ru.goidaai.test_backend.adapter.in.rest;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.goidaai.test_backend.dto.UserSearchResultDTO;
import ru.goidaai.test_backend.service.UserSearchService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserSearchService userSearchService;

    public UserController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResultDTO>> searchUsers(
            @RequestParam("q") String query,
            @AuthenticationPrincipal Jwt jwt) {
        List<UserSearchResultDTO> results = userSearchService.searchUsers(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/by-username")
    public ResponseEntity<UserSearchResultDTO> getUserByUsername(
            @RequestParam("username") String username,
            @AuthenticationPrincipal Jwt jwt) {
        UserSearchResultDTO user = userSearchService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
