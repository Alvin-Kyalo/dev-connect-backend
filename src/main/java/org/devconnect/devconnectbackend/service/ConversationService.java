package org.devconnect.devconnectbackend.service;

import org.devconnect.devconnectbackend.dto.ChatDTO;
import org.devconnect.devconnectbackend.model.Conversation;
import org.devconnect.devconnectbackend.model.Message;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.ConversationRepository;
import org.devconnect.devconnectbackend.repository.MessageRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get or create a conversation between two users
     */
    @Transactional
    public Conversation getOrCreateConversation(Integer userId1, Integer userId2) {
        return conversationRepository.findByUsers(userId1, userId2)
                .orElseGet(() -> {
                    User user1 = userRepository.findById(userId1)
                            .orElseThrow(() -> new RuntimeException("User1 not found"));
                    User user2 = userRepository.findById(userId2)
                            .orElseThrow(() -> new RuntimeException("User2 not found"));

                    Conversation conversation = new Conversation();
                    conversation.setUser1(user1);
                    conversation.setUser2(user2);
                    return conversationRepository.save(conversation);
                });
    }

    /**
     * Get all conversations for a user as ChatDTOs
     */
    public List<ChatDTO> getConversationsForUser(Integer userId) {
        List<Conversation> conversations = conversationRepository.findAllByUser(userId);
        List<ChatDTO> chatDTOs = new ArrayList<>();

        for (Conversation conversation : conversations) {
            // Determine the other user
            User otherUser = conversation.getUser1().getUserId().equals(userId)
                    ? conversation.getUser2()
                    : conversation.getUser1();

            // Get last message for preview
            Message lastMessage = messageRepository.findLastMessage(conversation.getConversationId());
            String lastMessagePreview = lastMessage != null ? lastMessage.getContent() : null;

            // Count unread messages for this user
            Integer unreadCount = messageRepository.findUnreadMessages(
                    conversation.getConversationId(),
                    userId
            ).size();

            String userName = otherUser.getFirstName() + " " + otherUser.getLastName();
            ChatDTO chatDTO = new ChatDTO(
                    conversation.getConversationId().longValue(),
                    otherUser.getUserId().longValue(),
                    userName,
                    null, // User model doesn't have avatar field
                    otherUser.getUserRole().name().toLowerCase(),
                    otherUser.getUserStatus().name().toLowerCase(),
                    lastMessagePreview,
                    lastMessage != null ? lastMessage.getCreatedAt() : conversation.getCreatedAt(),
                    unreadCount,
                    null // No projectId in new model
            );
            chatDTOs.add(chatDTO);
        }

        return chatDTOs;
    }

    /**
     * Get a conversation by ID (with permission check)
     */
    public Conversation getConversation(Integer conversationId, Integer userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Check that the user is a participant
        if (!conversation.getUser1().getUserId().equals(userId) &&
            !conversation.getUser2().getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: User is not a participant in this conversation");
        }

        return conversation;
    }
}

