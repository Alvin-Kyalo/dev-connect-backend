package org.devconnect.devconnectbackend.service;

import org.devconnect.devconnectbackend.dto.MessageDTO;
import org.devconnect.devconnectbackend.model.Conversation;
import org.devconnect.devconnectbackend.model.Message;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.MessageRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Message Service Tests")
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationService conversationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageService messageService;

    private Message testMessage;
    private Conversation testConversation;
    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test users
        sender = new User();
        sender.setUserId(1);
        sender.setFirstName("John");
        sender.setLastName("Doe");
        sender.setEmail("john@test.com");
        sender.setPasswordHash("password");
        sender.setUserRole(User.UserRole.CLIENT);

        receiver = new User();
        receiver.setUserId(2);
        receiver.setFirstName("Jane");
        receiver.setLastName("Smith");
        receiver.setEmail("jane@test.com");
        receiver.setPasswordHash("password");
        receiver.setUserRole(User.UserRole.DEVELOPER);

        // Create test conversation
        testConversation = new Conversation();
        testConversation.setConversationId(1);
        testConversation.setUser1(sender);
        testConversation.setUser2(receiver);
        testConversation.setCreatedAt(LocalDateTime.now());

        // Create test message
        testMessage = new Message();
        testMessage.setMessageId(1);
        testMessage.setConversation(testConversation);
        testMessage.setSender(sender);
        testMessage.setContent("Hello Jane!");
        testMessage.setStatus(Message.MessageStatus.SENT);
        testMessage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should send message successfully")
    void testSendMessage() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.of(receiver));
        when(conversationService.getOrCreateConversation(1, 2)).thenReturn(testConversation);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // Act
        MessageDTO result = messageService.sendMessage(1, 2, "Hello Jane!");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getSenderId());
        assertEquals(2L, result.getReceiverId());
        assertEquals("Hello Jane!", result.getText());
        assertEquals("sent", result.getStatus());

        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(2);
        verify(conversationService, times(1)).getOrCreateConversation(1, 2);
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("2"), eq("/queue/messages"), any(MessageDTO.class));
    }

    @Test
    @DisplayName("Should throw exception when sender not found")
    void testSendMessageSenderNotFound() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(1, 2, "Hello");
        });

        verify(userRepository, times(1)).findById(1);
        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when receiver not found")
    void testSendMessageReceiverNotFound() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(1, 2, "Hello");
        });

        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(2);
        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get messages in conversation")
    void testGetMessagesInConversation() {
        // Arrange
        Message message2 = new Message();
        message2.setMessageId(2);
        message2.setConversation(testConversation);
        message2.setSender(receiver);
        message2.setContent("Hi John!");
        message2.setStatus(Message.MessageStatus.SENT);
        message2.setCreatedAt(LocalDateTime.now().plusMinutes(1));

        List<Message> messages = Arrays.asList(testMessage, message2);

        when(conversationService.getConversation(1, 1)).thenReturn(testConversation);
        when(messageRepository.findByConversationConversationIdOrderByCreatedAtAsc(1))
                .thenReturn(messages);

        // Act
        List<MessageDTO> result = messageService.getMessagesInConversation(1, 1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getSenderId());
        assertEquals(2L, result.get(0).getReceiverId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(2L, result.get(1).getSenderId());
        assertEquals(1L, result.get(1).getReceiverId());

        verify(conversationService, times(1)).getConversation(1, 1);
        verify(messageRepository, times(1)).findByConversationConversationIdOrderByCreatedAtAsc(1);
    }

    @Test
    @DisplayName("Should get messages between users")
    void testGetMessagesBetweenUsers() {
        // Arrange
        List<Message> messages = Arrays.asList(testMessage);

        when(conversationService.getOrCreateConversation(1, 2)).thenReturn(testConversation);
        when(conversationService.getConversation(1, 1)).thenReturn(testConversation);
        when(messageRepository.findByConversationConversationIdOrderByCreatedAtAsc(1))
                .thenReturn(messages);

        // Act
        List<MessageDTO> result = messageService.getMessagesBetweenUsers(1, 2);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());

        verify(conversationService, times(1)).getOrCreateConversation(1, 2);
        verify(messageRepository, times(1)).findByConversationConversationIdOrderByCreatedAtAsc(1);
    }

    @Test
    @DisplayName("Should mark messages as read")
    void testMarkMessagesAsRead() {
        // Arrange
        List<Message> unreadMessages = Arrays.asList(testMessage);

        when(messageRepository.findUnreadMessages(1, 2)).thenReturn(unreadMessages);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // Act
        messageService.markMessagesAsRead(1, 2);

        // Assert
        verify(messageRepository, times(1)).findUnreadMessages(1, 2);
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("1"), eq("/queue/read-receipts"), any(MessageDTO.class));

        assertEquals(Message.MessageStatus.READ, testMessage.getStatus());
        assertNotNull(testMessage.getReadAt());
    }

    @Test
    @DisplayName("Should mark message as delivered")
    void testMarkMessageAsDelivered() {
        // Arrange
        when(messageRepository.findById(1)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // Act
        messageService.markMessageAsDelivered(1);

        // Assert
        verify(messageRepository, times(1)).findById(1);
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("1"), eq("/queue/delivery-receipts"), any(MessageDTO.class));

        assertEquals(Message.MessageStatus.DELIVERED, testMessage.getStatus());
        assertNotNull(testMessage.getDeliveredAt());
    }

    @Test
    @DisplayName("Should not mark already delivered message")
    void testMarkMessageAsDeliveredAlreadyDelivered() {
        // Arrange
        testMessage.setStatus(Message.MessageStatus.DELIVERED);
        when(messageRepository.findById(1)).thenReturn(Optional.of(testMessage));

        // Act
        messageService.markMessageAsDelivered(1);

        // Assert
        verify(messageRepository, times(1)).findById(1);
        verify(messageRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should throw exception when marking non-existent message as delivered")
    void testMarkMessageAsDeliveredNotFound() {
        // Arrange
        when(messageRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            messageService.markMessageAsDelivered(1);
        });

        verify(messageRepository, times(1)).findById(1);
        verify(messageRepository, never()).save(any());
    }
}

