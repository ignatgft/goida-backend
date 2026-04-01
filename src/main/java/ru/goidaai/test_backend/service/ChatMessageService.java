package ru.goidaai.test_backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.goidaai.test_backend.config.AppProperties;
import ru.goidaai.test_backend.dto.ChatMessageDTO;
import ru.goidaai.test_backend.dto.ConversationDTO;
import ru.goidaai.test_backend.exception.BadRequestException;
import ru.goidaai.test_backend.exception.ResourceNotFoundException;
import ru.goidaai.test_backend.model.ChatMessage;
import ru.goidaai.test_backend.model.User;
import ru.goidaai.test_backend.repository.ChatMessageRepository;
import ru.goidaai.test_backend.repository.UserRepository;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final SimpMessagingTemplate messagingTemplate;
    private final String uploadDir;

    public ChatMessageService(
            ChatMessageRepository chatMessageRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            SimpMessagingTemplate messagingTemplate,
            AppProperties appProperties) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.messagingTemplate = messagingTemplate;
        this.uploadDir = appProperties.getStorage().getUploadDir();
    }

    @Transactional
    public ChatMessageDTO sendMessage(String receiverId, String content, String type, MultipartFile file) {
        User currentUser = currentUserService.getCurrentUser();
        
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + receiverId));
        
        if (receiver.getId().equals(currentUser.getId())) {
            throw new BadRequestException("Cannot send message to yourself");
        }
        
        ChatMessage message = new ChatMessage();
        message.setSender(currentUser);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setType(ChatMessage.MessageType.valueOf(type.toUpperCase()));
        message.setIsRead(false);
        message.setSentAt(Instant.now());
        
        if (file != null && !file.isEmpty()) {
            processFile(message, file);
        }
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        messagingTemplate.convertAndSendToUser(
                receiverId,
                "/queue/messages",
                mapToDTO(savedMessage)
        );
        
        return mapToDTO(savedMessage);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getConversation(String otherUserId, int page, int size) {
        User currentUser = currentUserService.getCurrentUser();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").ascending());
        Page<ChatMessage> messages = chatMessageRepository.findConversationBetweenUsers(
                currentUser.getId(),
                otherUserId,
                pageable
        );
        
        return messages.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getUnreadMessages() {
        User currentUser = currentUserService.getCurrentUser();
        List<ChatMessage> messages = chatMessageRepository.findUnreadMessages(currentUser.getId());
        return messages.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(String otherUserId) {
        User currentUser = currentUserService.getCurrentUser();
        chatMessageRepository.markMessagesAsRead(currentUser.getId(), otherUserId);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversations() {
        User currentUser = currentUserService.getCurrentUser();
        List<String> senderIds = chatMessageRepository.findDistinctSenderIds(currentUser.getId());
        
        return senderIds.stream()
                .map(senderId -> {
                    User otherUser = userRepository.findById(senderId).orElse(null);
                    if (otherUser == null) return null;
                    
                    Page<ChatMessage> recentMessages = chatMessageRepository.findConversationBetweenUsers(
                            currentUser.getId(),
                            senderId,
                            PageRequest.of(0, 1, Sort.by("sentAt").descending())
                    );
                    
                    Long unreadCount = chatMessageRepository.findUnreadMessages(currentUser.getId())
                            .stream()
                            .filter(m -> m.getSender().getId().equals(senderId))
                            .count();
                    
                    ConversationDTO dto = new ConversationDTO();
                    dto.setUserId(otherUser.getId());
                    dto.setUserName(otherUser.getFullName());
                    dto.setUserAvatarUrl(otherUser.getAvatarUrl());
                    
                    if (!recentMessages.isEmpty()) {
                        ChatMessage lastMessage = recentMessages.getContent().get(0);
                        dto.setLastMessage(lastMessage.getContent());
                        dto.setLastMessageAt(lastMessage.getSentAt());
                    }
                    
                    dto.setUnreadCount(unreadCount);
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    private void processFile(ChatMessage message, MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path messagesDir = Path.of(uploadDir, "messages").toAbsolutePath().normalize();
            Files.createDirectories(messagesDir);
            Path filePath = messagesDir.resolve(fileName);
            
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = "/uploads/messages/" + fileName;
            message.setFileUrl(fileUrl);
            message.setFileName(file.getOriginalFilename());
            message.setFileContentType(file.getContentType());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload file", e);
        }
    }

    private ChatMessageDTO mapToDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullName());
        dto.setSenderAvatarUrl(message.getSender().getAvatarUrl());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setReceiverName(message.getReceiver().getFullName());
        dto.setContent(message.getContent());
        dto.setType(message.getType().name());
        dto.setFileUrl(message.getFileUrl());
        dto.setFileName(message.getFileName());
        dto.setFileContentType(message.getFileContentType());
        dto.setIsRead(message.getIsRead());
        dto.setSentAt(message.getSentAt());
        return dto;
    }
}
