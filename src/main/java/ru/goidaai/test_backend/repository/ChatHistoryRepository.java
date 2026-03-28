package ru.goidaai.test_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.goidaai.test_backend.model.ChatHistory;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, String> {
}
