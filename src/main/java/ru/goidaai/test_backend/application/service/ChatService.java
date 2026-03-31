package ru.goidaai.test_backend.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.application.port.out.ChatMessageRepositoryPort;
import ru.goidaai.test_backend.application.port.out.ConversationRepositoryPort;
import ru.goidaai.test_backend.domain.ChatMessage;
import ru.goidaai.test_backend.domain.Conversation;
import ru.goidaai.test_backend.domain.MessageType;

import java.time.Instant;
import java.util.List;

/**
 * Use case для управления сообщениями и конверсациями
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepositoryPort conversationRepository;
    private final ChatMessageRepositoryPort messageRepository;

    @Transactional
    public Conversation getOrCreateConversation(String user1Id, String user2Id) {
        return conversationRepository.findByUser1IdAndUser2Id(user1Id, user2Id)
            .or(() -> conversationRepository.findByUser1IdAndUser2Id(user2Id, user1Id))
            .orElseGet(() -> {
                Conversation conversation = Conversation.builder()
                    .user1Id(user1Id)
                    .user2Id(user2Id)
                    .lastMessage("")
                    .unreadCount(0)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
                return conversationRepository.save(conversation);
            });
    }

    @Transactional
    public ChatMessage sendMessage(String conversationId, String senderId, String recipientId, 
                                    String content, MessageType type) {
        ChatMessage message = ChatMessage.builder()
            .conversationId(conversationId)
            .senderId(senderId)
            .recipientId(recipientId)
            .content(content)
            .type(type)
            .isRead(false)
            .createdAt(Instant.now())
            .build();
        
        ChatMessage savedMessage = messageRepository.save(message);
        
        // Обновляем последнюю конверсацию
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setLastMessage(content);
        conversation.setLastMessageAt(Instant.now());
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);
        
        return savedMessage;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(String conversationId, int limit, int offset) {
        return messageRepository.findByConversationId(conversationId, limit, offset);
    }

    @Transactional(readOnly = true)
    public List<Conversation> getUserConversations(String userId) {
        return conversationRepository.findByUserId(userId);
    }

    @Transactional
    public void markMessageAsRead(String messageId) {
        messageRepository.markAsRead(messageId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getUnreadMessages(String userId) {
        return messageRepository.findUnreadMessages(userId);
    }
}
