package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Insert("""
        INSERT INTO message
          (receiver_type, receiver_id, sender_type, sender_id, message_type, content, related_id, is_read, created_at)
        VALUES
          (#{receiverType}, #{receiverId}, #{senderType}, #{senderId}, #{messageType}, #{content}, #{relatedId}, #{isRead}, #{createdAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "messageId")
    int insertMessage(Message message); // MOD: 返回 int（可选，但更规范）

    @Select("""
        SELECT * FROM message
        WHERE receiver_type = #{receiverType} AND receiver_id = #{receiverId}
        ORDER BY created_at DESC
        """)
    List<Message> selectMessagesByReceiver(@Param("receiverType") String receiverType,
                                           @Param("receiverId") Long receiverId);

    @Select("""
        SELECT * FROM message
        WHERE receiver_type = #{receiverType} AND receiver_id = #{receiverId}
        ORDER BY created_at DESC
        LIMIT #{offset}, #{limit}
        """)
    List<Message> selectMessagesByReceiverWithPaging(@Param("receiverType") String receiverType,
                                                     @Param("receiverId") Long receiverId,
                                                     @Param("offset") int offset,
                                                     @Param("limit") int limit);

    @Select("""
        SELECT COUNT(*) FROM message
        WHERE receiver_type = #{receiverType} AND receiver_id = #{receiverId} AND is_read = 0
        """)
    int selectUnreadCount(@Param("receiverType") String receiverType,
                          @Param("receiverId") Long receiverId);

    // ================= MOD: 防越权 =================

    @Update("""
        UPDATE message
        SET is_read = 1
        WHERE message_id = #{messageId}
          AND receiver_type = #{receiverType}
          AND receiver_id = #{receiverId}
          AND is_read = 0
        """)
    int updateMessageAsReadForReceiver(@Param("messageId") Long messageId,
                                       @Param("receiverType") String receiverType,
                                       @Param("receiverId") Long receiverId);

    @Update("""
        UPDATE message
        SET is_read = 1
        WHERE receiver_type = #{receiverType}
          AND receiver_id = #{receiverId}
          AND is_read = 0
        """)
    int updateMessagesAsRead(@Param("receiverType") String receiverType,
                             @Param("receiverId") Long receiverId);
}
