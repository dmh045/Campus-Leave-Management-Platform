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
     * 标记消息为已读（只能标记“属于当前登录用户”的消息）
     */
    void markAsRead(Long messageId, String receiverType, Long receiverId); // MOD

    /**
     * 批量标记当前登录用户的消息为已读
     * @return 本次更新的行数
     */
    int batchMarkAsRead(String receiverType, Long receiverId); // MOD
}
