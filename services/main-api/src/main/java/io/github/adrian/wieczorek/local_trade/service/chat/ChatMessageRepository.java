package io.github.adrian.wieczorek.local_trade.service.chat;

import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    @Query("SELECT m FROM ChatMessageEntity m WHERE " +
            "(m.sender = :u1 AND m.recipient = :u2) OR " +
            "(m.sender = :u2 AND m.recipient = :u1) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessageEntity> findBySenderAndRecipient(UsersEntity user1, UsersEntity user2);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessageEntity m SET m.isRead = true " +
            "WHERE m.sender = :sender AND m.recipient = :recipient AND m.isRead = false")
    void markAllAsRead(@Param("sender") UsersEntity sender, @Param("recipient") UsersEntity recipient);
}
