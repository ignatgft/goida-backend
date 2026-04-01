package ru.goidaai.test_backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.model.ChatHistory;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, String> {

    List<ChatHistory> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<ChatHistory> findByIdAndUserId(String id, String userId);
}
