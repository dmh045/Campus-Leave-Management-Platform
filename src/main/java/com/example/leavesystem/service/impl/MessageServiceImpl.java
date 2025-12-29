package com.example.leavesystem.service.impl;

import com.example.leavesystem.entity.Message;
import com.example.leavesystem.mapper.MessageMapper;
import com.example.leavesystem.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public void sendLeaveStatusChangeMessage(Long leaveId, String oldStatus, String newStatus, Long receiverId) {
        Message message = new Message();
        message.setReceiverType("STUDENT");
        message.setReceiverId(receiverId);
        message.setSenderType("SYSTEM");
        message.setMessageType("LEAVE_STATUS_CHANGE");
        message.setContent("您的请假单(ID:" + leaveId + ")状态已从" + oldStatus + "变更为" + newStatus);
        message.setRelatedId(leaveId);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insertMessage(message);
    }

    @Override
    public void sendTeacherConfirmMessage(Long leaveId, Long receiverId, Long teacherId, String remark) {
        Message message = new Message();
        message.setReceiverType("STUDENT");
        message.setReceiverId(receiverId);
        message.setSenderType("STAFF");
        message.setSenderId(teacherId);
        message.setMessageType("TEACHER_CONFIRM");
        message.setContent("教师已确认您的请假单(ID:" + leaveId + ")，备注：" + remark);
        message.setRelatedId(leaveId);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insertMessage(message);
    }

    @Override
    public void sendCounselorApproveMessage(Long leaveId, Long receiverId, Long counselorId, String action) {
        Message message = new Message();
        message.setReceiverType("STUDENT");
        message.setReceiverId(receiverId);
        message.setSenderType("STAFF");
        message.setSenderId(counselorId);
        message.setMessageType("COUNSELOR_APPROVE");

        String actionText = "AGREE".equals(action) ? "批准" : "拒绝";
        message.setContent("辅导员已" + actionText + "您的请假单(ID:" + leaveId + ")");

        message.setRelatedId(leaveId);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insertMessage(message);
    }

    @Override
    public List<Message> getMessageList(String receiverType, Long receiverId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        return messageMapper.selectMessagesByReceiverWithPaging(receiverType, receiverId, offset, size);
    }

    @Override
    public int getUnreadCount(String receiverType, Long receiverId) {
        return messageMapper.selectUnreadCount(receiverType, receiverId);
    }

    // ================= MOD：防越权 =================

    @Override
    public void markAsRead(Long messageId, String receiverType, Long receiverId) {
        // 只允许把“属于自己的消息”标记为已读
        int updated = messageMapper.updateMessageAsReadForReceiver(messageId, receiverType, receiverId);
        if (updated == 0) {
            throw new IllegalStateException("消息不存在或无权限操作");
        }
    }

    @Override
    public int batchMarkAsRead(String receiverType, Long receiverId) {
        // 返回更新条数，方便测试/前端刷新未读数
        return messageMapper.updateMessagesAsRead(receiverType, receiverId);
    }
}
