package ru.goidaai.test_backend.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.goidaai.test_backend.dto.ChatSessionDTO;
import ru.goidaai.test_backend.exception.ResourceNotFoundException;
import ru.goidaai.test_backend.model.ChatMessage;
import ru.goidaai.test_backend.model.ChatSession;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.repository.ChatSessionRepository;

@Service
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final CurrentUserService currentUserService;

    public ChatSessionService(
            ChatSessionRepository chatSessionRepository,
            CurrentUserService currentUserService) {
        this.chatSessionRepository = chatSessionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<ChatSessionDTO> getAllSessions() {
        User user = currentUserService.getCurrentUser();
        List<ChatSession> sessions = chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
        
        return sessions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChatSessionDTO getSessionById(String sessionId) {
        User user = currentUserService.getCurrentUser();
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        
        return mapToDTO(session);
    }

    @Transactional
    public ChatSessionDTO createSession(String title, String sessionType) {
        User user = currentUserService.getCurrentUser();
        
        ChatSession session = new ChatSession();
        session.setUser(user);
        session.setTitle(title != null && !title.isBlank() ? title : (sessionType != null && sessionType.equals("AI") ? "New AI Chat" : "New Chat"));
        session.setSessionType(sessionType);
        session.setCreatedAt(Instant.now());
        session.setUpdatedAt(Instant.now());
        
        ChatSession saved = chatSessionRepository.save(session);
        return mapToDTO(saved);
    }

    @Transactional
    public ChatSessionDTO updateSessionTitle(String sessionId, String newTitle) {
        User user = currentUserService.getCurrentUser();
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        
        session.setTitle(newTitle);
        session.setUpdatedAt(Instant.now());
        
        ChatSession updated = chatSessionRepository.save(session);
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteSession(String sessionId) {
        User user = currentUserService.getCurrentUser();
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        
        chatSessionRepository.delete(session);
    }

    @Transactional
    public ChatSessionDTO updateSessionPreview(String sessionId, String previewMessage) {
        User user = currentUserService.getCurrentUser();
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        
        session.setPreviewMessage(previewMessage);
        session.setUpdatedAt(Instant.now());
        
        ChatSession updated = chatSessionRepository.save(session);
        return mapToDTO(updated);
    }

    private ChatSessionDTO mapToDTO(ChatSession session) {
        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setId(session.getId());
        dto.setTitle(session.getTitle());
        dto.setPreviewMessage(session.getPreviewMessage());
        dto.setSessionType(session.getSessionType());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        
        if (session.getMessages() != null) {
            List<ChatMessage> sortedMessages = session.getMessages().stream()
                    .sorted(Comparator.comparing(ChatMessage::getSentAt))
                    .collect(Collectors.toList());
            
            dto.setMessages(sortedMessages.stream()
                    .map(msg -> {
                        var msgDto = new ru.goidaai.test_backend.dto.ChatMessageDTO();
                        msgDto.setId(msg.getId());
                        msgDto.setSenderId(msg.getSender().getId());
                        msgDto.setSenderName(msg.getSender().getFullName());
                        msgDto.setSenderAvatarUrl(msg.getSender().getAvatarUrl());
                        msgDto.setContent(msg.getContent());
                        msgDto.setType(msg.getType().name());
                        msgDto.setFileUrl(msg.getFileUrl());
                        msgDto.setFileName(msg.getFileName());
                        msgDto.setIsRead(msg.getIsRead());
                        msgDto.setSentAt(msg.getSentAt());
                        
                        if (msg.getReplyTo() != null) {
                            msgDto.setReplyToId(msg.getReplyTo().getId());
                            msgDto.setReplyToContent(msg.getReplyTo().getContent());
                        }
                        
                        return msgDto;
                    })
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}
