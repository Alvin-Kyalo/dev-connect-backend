package org.devconnect.devconnectbackend.repository;

import org.devconnect.devconnectbackend.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    // Retrieve a conversation between two users
    @Query("""
           SELECT c FROM Conversation c
           WHERE (c.user1.userId = :userId1 AND c.user2.userId = :userId2)
              OR (c.user1.userId = :userId2 AND c.user2.userId = :userId1)
           """)
    Optional<Conversation> findByUsers(Integer userId1, Integer userId2);

    // Get all conversations for one user
    @Query("""
           SELECT c FROM Conversation c
           WHERE c.user1.userId = :userId
              OR c.user2.userId = :userId
           ORDER BY c.createdAt DESC
           """)
    List<Conversation> findAllByUser(Integer userId);
}
