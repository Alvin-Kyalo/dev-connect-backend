package org.devconnect.devconnectbackend.service;

import org.devconnect.devconnectbackend.dto.ChatDTO;
import org.devconnect.devconnectbackend.model.Conversation;
import org.devconnect.devconnectbackend.model.Message;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.ConversationRepository;
import org.devconnect.devconnectbackend.repository.MessageRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Conversation Service Tests")
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConversationService conversationService;

    private User user1;
    private User user2;
    private Conversation testConversation;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test users
        user1 = new User();
        user1.setUserId(1);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john@test.com");
        user1.setUserRole(User.UserRole.CLIENT);
        user1.setUserStatus(User.UserStatus.ONLINE);

        user2 = new User();
        user2.setUserId(2);
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane@test.com");
        user2.setUserRole(User.UserRole.DEVELOPER);
        user2.setUserStatus(User.UserStatus.ONLINE);

        // Create test conversation
        testConversation = new Conversation();
        testConversation.setConversationId(1);
        testConversation.setUser1(user1);
        testConversation.setUser2(user2);
        testConversation.setCreatedAt(LocalDateTime.now());

        // Create test message
        testMessage = new Message();
        testMessage.setMessageId(1);
        testMessage.setConversation(testConversation);
        testMessage.setSender(user1);
        testMessage.setContent("Hello!");
        testMessage.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should get existing conversation")
    void testGetOrCreateConversationExisting() {
        // Arrange
        when(conversationRepository.findByUsers(1, 2)).thenReturn(Optional.of(testConversation));

        // Act
        Conversation result = conversationService.getOrCreateConversation(1, 2);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getConversationId());
        verify(conversationRepository, times(1)).findByUsers(1, 2);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create new conversation when not exists")
    void testGetOrCreateConversationNew() {
        // Arrange
        when(conversationRepository.findByUsers(1, 2)).thenReturn(Optional.empty());
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);

        // Act
        Conversation result = conversationService.getOrCreateConversation(1, 2);

        // Assert
        assertNotNull(result);
        verify(conversationRepository, times(1)).findByUsers(1, 2);
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(2);
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    @DisplayName("Should throw exception when user1 not found")
    void testGetOrCreateConversationUser1NotFound() {
        // Arrange
        when(conversationRepository.findByUsers(1, 2)).thenReturn(Optional.empty());
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            conversationService.getOrCreateConversation(1, 2);
        });

        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user2 not found")
    void testGetOrCreateConversationUser2NotFound() {
        // Arrange
        when(conversationRepository.findByUsers(1, 2)).thenReturn(Optional.empty());
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            conversationService.getOrCreateConversation(1, 2);
        });

        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get conversations for user")
    void testGetConversationsForUser() {
        // Arrange
        List<Conversation> conversations = Arrays.asList(testConversation);
        when(conversationRepository.findAllByUser(1)).thenReturn(conversations);
        when(messageRepository.findLastMessage(1)).thenReturn(testMessage);
        when(messageRepository.findUnreadMessages(1, 1)).thenReturn(Collections.emptyList());

        // Act
        List<ChatDTO> result = conversationService.getConversationsForUser(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ChatDTO chatDTO = result.get(0);
        assertEquals(1L, chatDTO.getId());
        assertEquals(2L, chatDTO.getUserId());
        assertEquals("Jane Smith", chatDTO.getUserName());
        assertEquals("developer", chatDTO.getUserRole());
        assertEquals("online", chatDTO.getUserStatus());
        assertEquals("Hello!", chatDTO.getLastMessage());
        assertEquals(0, chatDTO.getUnreadCount());

        verify(conversationRepository, times(1)).findAllByUser(1);
        verify(messageRepository, times(1)).findLastMessage(1);
        verify(messageRepository, times(1)).findUnreadMessages(1, 1);
    }

    @Test
    @DisplayName("Should handle conversation with no messages")
    void testGetConversationsForUserNoMessages() {
        // Arrange
        List<Conversation> conversations = Arrays.asList(testConversation);
        when(conversationRepository.findAllByUser(1)).thenReturn(conversations);
        when(messageRepository.findLastMessage(1)).thenReturn(null);
        when(messageRepository.findUnreadMessages(1, 1)).thenReturn(Collections.emptyList());

        // Act
        List<ChatDTO> result = conversationService.getConversationsForUser(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        ChatDTO chatDTO = result.get(0);
        assertNull(chatDTO.getLastMessage());
        assertNotNull(chatDTO.getLastMessageTime()); // Should use conversation createdAt
    }

    @Test
    @DisplayName("Should get conversation by ID with valid user")
    void testGetConversation() {
        // Arrange
        when(conversationRepository.findById(1)).thenReturn(Optional.of(testConversation));

        // Act
        Conversation result = conversationService.getConversation(1, 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getConversationId());
        verify(conversationRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw exception when conversation not found")
    void testGetConversationNotFound() {
        // Arrange
        when(conversationRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            conversationService.getConversation(1, 1);
        });

        verify(conversationRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw exception when user not participant")
    void testGetConversationAccessDenied() {
        // Arrange
        when(conversationRepository.findById(1)).thenReturn(Optional.of(testConversation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            conversationService.getConversation(1, 999); // User 999 is not a participant
        });

        assertTrue(exception.getMessage().contains("Access denied"));
        verify(conversationRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should allow user2 to access conversation")
    void testGetConversationUser2Access() {
        // Arrange
        when(conversationRepository.findById(1)).thenReturn(Optional.of(testConversation));

        // Act
        Conversation result = conversationService.getConversation(1, 2);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getConversationId());
    }

    @Test
    @DisplayName("Should return empty list when user has no conversations")
    void testGetConversationsForUserEmpty() {
        // Arrange
        when(conversationRepository.findAllByUser(1)).thenReturn(Collections.emptyList());

        // Act
        List<ChatDTO> result = conversationService.getConversationsForUser(1);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(conversationRepository, times(1)).findAllByUser(1);
    }

    @Test
    @DisplayName("Should count unread messages correctly")
    void testGetConversationsForUserWithUnreadMessages() {
        // Arrange
        Message unreadMessage1 = new Message();
        Message unreadMessage2 = new Message();
        List<Message> unreadMessages = Arrays.asList(unreadMessage1, unreadMessage2);

        List<Conversation> conversations = Arrays.asList(testConversation);
        when(conversationRepository.findAllByUser(1)).thenReturn(conversations);
        when(messageRepository.findLastMessage(1)).thenReturn(testMessage);
        when(messageRepository.findUnreadMessages(1, 1)).thenReturn(unreadMessages);

        // Act
        List<ChatDTO> result = conversationService.getConversationsForUser(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getUnreadCount());
    }
}

