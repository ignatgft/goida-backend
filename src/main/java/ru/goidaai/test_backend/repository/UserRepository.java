package ru.goidaai.test_backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.goidaai.test_backend.model.User;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByGoogleSubject(String googleSubject);
}
