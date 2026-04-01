package ru.goidaai.test_backend.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.goidaai.test_backend.model.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    Page<ChatMessage> findBySenderIdAndReceiverId(String senderId, String receiverId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2) OR (m.sender.id = :userId2 AND m.receiver.id = :userId1) ORDER BY m.sentAt ASC")
    Page<ChatMessage> findConversationBetweenUsers(@Param("userId1") String userId1, @Param("userId2") String userId2, Pageable pageable);

    List<ChatMessage> findBySenderIdAndReceiverIdOrderBySentAtAsc(String senderId, String receiverId);

    @Query("SELECT m FROM ChatMessage m WHERE m.receiver.id = :userId AND m.isRead = false ORDER BY m.sentAt DESC")
    List<ChatMessage> findUnreadMessages(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId")
    void markMessagesAsRead(@Param("receiverId") String receiverId, @Param("senderId") String senderId);

    @Query("SELECT DISTINCT m.sender.id FROM ChatMessage m WHERE m.receiver.id = :userId")
    List<String> findDistinctSenderIds(@Param("userId") String userId);
}
