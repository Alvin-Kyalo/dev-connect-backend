package org.devconnect.devconnectbackend.repository;

import org.devconnect.devconnectbackend.model.Message;
import org.devconnect.devconnectbackend.model.Message.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    // Get all messages for a conversation (sorted oldest → newest)
    List<Message> findByConversationConversationIdOrderByCreatedAtAsc(Integer conversationId);

    // Get unread messages sent to a specific user
    @Query("""
           SELECT m FROM Message m
           WHERE m.conversation.conversationId = :conversationId
             AND m.sender.userId <> :userId
             AND m.status <> 'READ'
           """)
    List<Message> findUnreadMessages(Integer conversationId, Integer userId);

    // Get last message in a conversation (useful for showing chat previews)
    @Query("""
           SELECT m FROM Message m
           WHERE m.conversation.conversationId = :conversationId
           ORDER BY m.createdAt DESC
           LIMIT 1
           """)
    Message findLastMessage(Integer conversationId);
}
