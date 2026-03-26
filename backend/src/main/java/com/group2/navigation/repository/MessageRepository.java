package com.group2.navigation.repository;

import com.group2.navigation.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId ORDER BY m.timestamp DESC")
    List<Message> findByReceiverId(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId OR m.receiver.id = :receiverId ORDER BY m.timestamp DESC")
    List<Message> findBySenderIdOrReceiverId(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    void deleteAllInvolvingUser(@Param("userId") Long userId);
}
