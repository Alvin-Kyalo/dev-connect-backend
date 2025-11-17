package org.devconnect.devconnectbackend.service;

import org.devconnect.devconnectbackend.dto.MessageDTO;
import org.devconnect.devconnectbackend.model.Conversation;
import org.devconnect.devconnectbackend.model.Message;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.MessageRepository;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send a message from one user to another
     */
    @Transactional
    public MessageDTO sendMessage(Integer senderId, Integer receiverId, String content) {
        // Validate users exist
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Get or create conversation
        Conversation conversation = conversationService.getOrCreateConversation(senderId, receiverId);

        // Create and save message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message.setStatus(Message.MessageStatus.SENT);
        message.setCreatedAt(LocalDateTime.now());

        message = messageRepository.save(message);

        // Convert to DTO
        MessageDTO messageDTO = convertToDTO(message, receiverId);

        // Send via WebSocket to receiver
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                messageDTO
        );

        return messageDTO;
    }

    /**
     * Get all messages in a conversation
     */
    public List<MessageDTO> getMessagesInConversation(Integer conversationId, Integer requestingUserId) {
        // Verify user is a participant
        Conversation conversation = conversationService.getConversation(conversationId, requestingUserId);

        List<Message> messages = messageRepository.findByConversationConversationIdOrderByCreatedAtAsc(conversationId);
        List<MessageDTO> messageDTOs = new ArrayList<>();

        // Determine the other user in the conversation
        Integer otherUserId = getOtherUserId(conversation, requestingUserId);

        for (Message message : messages) {
            // Determine receiverId for DTO (opposite of sender)
            Integer receiverId = message.getSender().getUserId().equals(requestingUserId)
                                  ? otherUserId
                                  : requestingUserId;
            messageDTOs.add(convertToDTO(message, receiverId));
        }

        return messageDTOs;
    }

    /**
     * Get messages between two users (creates conversation if needed)
     */
    public List<MessageDTO> getMessagesBetweenUsers(Integer userId1, Integer userId2) {
        Conversation conversation = conversationService.getOrCreateConversation(userId1, userId2);
        return getMessagesInConversation(conversation.getConversationId(), userId1);
    }

    /**
     * Mark messages as read in a conversation
     */
    @Transactional
    public void markMessagesAsRead(Integer conversationId, Integer readerId) {
        List<Message> unreadMessages = messageRepository.findUnreadMessages(conversationId, readerId);

        for (Message message : unreadMessages) {
            message.setStatus(Message.MessageStatus.READ);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);

            // Notify sender about read receipt
            Integer senderId = message.getSender().getUserId();
            MessageDTO messageDTO = convertToDTO(message, readerId);
            messagingTemplate.convertAndSendToUser(
                    senderId.toString(),
                    "/queue/read-receipts",
                    messageDTO
            );
        }
    }

    /**
     * Mark message as delivered
     */
    @Transactional
    public void markMessageAsDelivered(Integer messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.getStatus() == Message.MessageStatus.SENT) {
            message.setStatus(Message.MessageStatus.DELIVERED);
            message.setDeliveredAt(LocalDateTime.now());
            messageRepository.save(message);

            // Get conversation to determine receiver
            Conversation conversation = message.getConversation();
            Integer senderId = message.getSender().getUserId();
            Integer receiverId = getOtherUserId(conversation, senderId);

            // Notify sender about delivery
            MessageDTO messageDTO = convertToDTO(message, receiverId);
            messagingTemplate.convertAndSendToUser(
                    senderId.toString(),
                    "/queue/delivery-receipts",
                    messageDTO
            );
        }
    }

    /**
     * Helper: Get the other user in a conversation
     */
    private Integer getOtherUserId(Conversation conversation, Integer userId) {
        if (conversation.getUser1().getUserId().equals(userId)) {
            return conversation.getUser2().getUserId();
        } else if (conversation.getUser2().getUserId().equals(userId)) {
            return conversation.getUser1().getUserId();
        }
        throw new RuntimeException("User is not a participant in this conversation");
    }

    /**
     * Convert Message entity to DTO
     */
    private MessageDTO convertToDTO(Message message, Integer receiverId) {
        return new MessageDTO(
                message.getMessageId().longValue(),
                message.getSender().getUserId().longValue(),
                receiverId.longValue(),
                message.getContent(),
                message.getStatus().name().toLowerCase(),
                message.getCreatedAt(),
                null // No projectId in current model
        );
    }
}

