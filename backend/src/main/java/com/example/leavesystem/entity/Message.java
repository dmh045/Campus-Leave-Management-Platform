package com.example.leavesystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {

    private Long messageId;
    private String receiverType;    // STUDENT / STAFF / COUNSELOR
    private Long receiverId;        // 对应的用户ID
    private String senderType;      // SYSTEM / STAFF / COUNSELOR
    private Long senderId;          // 发送者ID
    private String messageType;     // LEAVE_STATUS_CHANGE / TEACHER_CONFIRM / COUNSELOR_APPROVE
    private String content;         // 消息内容
    private Long relatedId;         // 关联的请假单ID或其他业务ID
    private Boolean isRead;         // 是否已读
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}