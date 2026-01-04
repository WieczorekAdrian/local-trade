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
    List<ChatMessageEntity> findBySenderAndRecipient(@Param("u1")UsersEntity user1,@Param("u2") UsersEntity user2);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessageEntity m SET m.isRead = true " +
            "WHERE m.sender = :sender AND m.recipient = :recipient AND m.isRead = false")
    void markAllAsRead(@Param("sender") UsersEntity sender, @Param("recipient") UsersEntity recipient);

    @Query("SELECT m FROM ChatMessageEntity m WHERE m.id IN (" +
            "  SELECT MAX(m2.id) FROM ChatMessageEntity m2 " +
            "  WHERE m2.sender = :user OR m2.recipient = :user " +
            "  GROUP BY CASE " +
            "    WHEN m2.sender = :user THEN m2.recipient " +
            "    ELSE m2.sender END" +
            ") ORDER BY m.timestamp DESC")
    List<ChatMessageEntity> findLastMessagesPerConversation(@Param("user") UsersEntity user);

    @Query("SELECT COUNT(m) FROM ChatMessageEntity m " +
            "WHERE m.sender = :partner AND m.recipient = :me AND m.isRead = false")
    long countUnreadFromPartner(@Param("partner") UsersEntity partner, @Param("me") UsersEntity me);

    @Query("SELECT COUNT(m) FROM ChatMessageEntity m WHERE m.recipient = :user AND m.isRead = false")
    long countTotalUnread(@Param("user") UsersEntity user);
}
