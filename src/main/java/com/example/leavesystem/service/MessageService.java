package com.example.leavesystem.service;

import com.example.leavesystem.entity.Message;

import java.util.List;

public interface MessageService {

    /**
     * 发送请假状态变更消息
     */
    void sendLeaveStatusChangeMessage(Long leaveId, String oldStatus, String newStatus, Long receiverId);

    /**
     * 发送教师确认消息
     */
    void sendTeacherConfirmMessage(Long leaveId, Long receiverId, Long teacherId, String remark);

    /**
     * 发送辅导员批准消息
     */
    void sendCounselorApproveMessage(Long leaveId, Long receiverId, Long counselorId, String action);

    /**
     * 获取消息列表
     */
    List<Message> getMessageList(String receiverType, Long receiverId, Integer page, Integer size);

    /**
     * 获取未读消息数量
     */
    int getUnreadCount(String receiverType, Long receiverId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId);

    /**
     * 批量标记消息为已读
     */
    void batchMarkAsRead(String receiverType, Long receiverId);
}