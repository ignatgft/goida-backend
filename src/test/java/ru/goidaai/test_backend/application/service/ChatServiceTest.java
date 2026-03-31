package ru.goidaai.test_backend.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.goidaai.test_backend.application.port.out.ChatMessageRepositoryPort;
import ru.goidaai.test_backend.application.port.out.ConversationRepositoryPort;
import ru.goidaai.test_backend.domain.ChatMessage;
import ru.goidaai.test_backend.domain.Conversation;
import ru.goidaai.test_backend.domain.MessageType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ConversationRepositoryPort conversationRepository;

    @Mock
    private ChatMessageRepositoryPort messageRepository;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(conversationRepository, messageRepository);
    }

    @Test
    void getOrCreateConversation_shouldReturnExistingConversation() {
        // Arrange
        String user1Id = "user-1";
        String user2Id = "user-2";
        Conversation existingConversation = Conversation.builder()
            .id("conv-123")
            .user1Id(user1Id)
            .user2Id(user2Id)
            .lastMessage("Hello")
            .lastMessageAt(Instant.now())
            .unreadCount(1)
            .build();

        when(conversationRepository.findByUser1IdAndUser2Id(user1Id, user2Id))
            .thenReturn(Optional.of(existingConversation));

        // Act
        Conversation result = chatService.getOrCreateConversation(user1Id, user2Id);

        // Assert
        assertNotNull(result);
        assertEquals("conv-123", result.getId());
        verify(conversationRepository, times(1)).findByUser1IdAndUser2Id(user1Id, user2Id);
        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void getOrCreateConversation_shouldCreateNewConversation() {
        // Arrange
        String user1Id = "user-1";
        String user2Id = "user-2";

        when(conversationRepository.findByUser1IdAndUser2Id(anyString(), anyString()))
            .thenReturn(Optional.empty());

        Conversation newConversation = Conversation.builder()
            .id("conv-new")
            .user1Id(user1Id)
            .user2Id(user2Id)
            .build();

        when(conversationRepository.save(any(Conversation.class))).thenReturn(newConversation);

        // Act
        Conversation result = chatService.getOrCreateConversation(user1Id, user2Id);

        // Assert
        assertNotNull(result);
        assertEquals("conv-new", result.getId());
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void sendMessage_shouldSaveMessageAndUpdateConversation() {
        // Arrange
        String conversationId = "conv-123";
        String senderId = "user-1";
        String recipientId = "user-2";
        String content = "Hello!";

        ChatMessage savedMessage = ChatMessage.builder()
            .id("msg-123")
            .conversationId(conversationId)
            .senderId(senderId)
            .recipientId(recipientId)
            .content(content)
            .type(MessageType.TEXT)
            .isRead(false)
            .createdAt(Instant.now())
            .build();

        Conversation conversation = Conversation.builder()
            .id(conversationId)
            .user1Id(senderId)
            .user2Id(recipientId)
            .build();

        when(messageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        // Act
        ChatMessage result = chatService.sendMessage(conversationId, senderId, recipientId, content, MessageType.TEXT);

        // Assert
        assertNotNull(result);
        assertEquals("msg-123", result.getId());
        assertEquals(content, result.getContent());
        assertFalse(result.getIsRead());
        verify(messageRepository, times(1)).save(any(ChatMessage.class));
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void getMessages_shouldReturnListOfMessages() {
        // Arrange
        String conversationId = "conv-123";
        List<ChatMessage> messages = List.of(
            ChatMessage.builder().id("msg-1").conversationId(conversationId).content("Message 1").type(MessageType.TEXT).senderId("user-1").recipientId("user-2").isRead(true).createdAt(Instant.now()).build(),
            ChatMessage.builder().id("msg-2").conversationId(conversationId).content("Message 2").type(MessageType.TEXT).senderId("user-2").recipientId("user-1").isRead(false).createdAt(Instant.now()).build()
        );

        when(messageRepository.findByConversationId(conversationId, 20, 0)).thenReturn(messages);

        // Act
        List<ChatMessage> result = chatService.getMessages(conversationId, 20, 0);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(messageRepository, times(1)).findByConversationId(conversationId, 20, 0);
    }

    @Test
    void getUserConversations_shouldReturnListOfConversations() {
        // Arrange
        String userId = "user-123";
        List<Conversation> conversations = List.of(
            Conversation.builder().id("conv-1").user1Id(userId).user2Id("user-2").lastMessage("Hi").unreadCount(1).build(),
            Conversation.builder().id("conv-2").user1Id(userId).user2Id("user-3").lastMessage("Hello").unreadCount(0).build()
        );

        when(conversationRepository.findByUserId(userId)).thenReturn(conversations);

        // Act
        List<Conversation> result = chatService.getUserConversations(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(conversationRepository, times(1)).findByUserId(userId);
    }

    @Test
    void markMessageAsRead_shouldCallRepository() {
        // Arrange
        String messageId = "msg-123";

        // Act
        chatService.markMessageAsRead(messageId);

        // Assert
        verify(messageRepository, times(1)).markAsRead(messageId);
    }

    @Test
    void getUnreadMessages_shouldReturnListOfUnreadMessages() {
        // Arrange
        String userId = "user-123";
        List<ChatMessage> unreadMessages = List.of(
            ChatMessage.builder().id("msg-1").conversationId("conv-1").content("Unread 1").type(MessageType.TEXT).senderId("user-2").recipientId(userId).isRead(false).createdAt(Instant.now()).build(),
            ChatMessage.builder().id("msg-2").conversationId("conv-1").content("Unread 2").type(MessageType.TEXT).senderId("user-2").recipientId(userId).isRead(false).createdAt(Instant.now()).build()
        );

        when(messageRepository.findUnreadMessages(userId)).thenReturn(unreadMessages);

        // Act
        List<ChatMessage> result = chatService.getUnreadMessages(userId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(messageRepository, times(1)).findUnreadMessages(userId);
    }
}
