package com.team.socialnetwork.repository;

import com.team.socialnetwork.entity.Message;
import com.team.socialnetwork.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderAndReceiverOrderByCreatedAtAsc(User sender, User receiver);

    @Query("SELECT m FROM Message m WHERE (m.sender.id = :me AND m.receiver.id = :other) OR " +
            "(m.sender.id = :other AND m.receiver.id = :me) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("me") Long me, @Param("other") Long other);

    @Query("SELECT DISTINCT " +
           "CASE WHEN m.sender.id = :userId THEN m.receiver.id ELSE m.sender.id END " +
           "FROM Message m " +
           "WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Long> findChatUserIds(@Param("userId") Long userId);
}
