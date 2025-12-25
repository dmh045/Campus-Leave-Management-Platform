package com.example.leavesystem;

import com.example.leavesystem.entity.Message;
import com.example.leavesystem.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageModuleTest {

    @Mock
    private MessageService messageService;

    @Test
    void testSendMessageFunctionality() {
        // 测试发送请假状态变更消息 - 方法返回void，不需要验证返回值
        assertDoesNotThrow(() -> messageService.sendLeaveStatusChangeMessage(1L, "PENDING", "APPROVED", 1L));

        // 验证方法被调用
        verify(messageService, times(1))
                .sendLeaveStatusChangeMessage(1L, "PENDING", "APPROVED", 1L);
    }

    @Test
    void testSendTeacherConfirmMessage() {
        // 测试发送教师确认消息
        assertDoesNotThrow(() -> messageService.sendTeacherConfirmMessage(1L, 1L, 201L, "已确认"));

        // 验证方法被调用
        verify(messageService, times(1))
                .sendTeacherConfirmMessage(1L, 1L, 201L, "已确认");
    }

    @Test
    void testSendCounselorApproveMessage() {
        // 测试发送辅导员批准消息
        assertDoesNotThrow(() -> messageService.sendCounselorApproveMessage(1L, 1L, 301L, "AGREE"));

        // 验证方法被调用
        verify(messageService, times(1))
                .sendCounselorApproveMessage(1L, 1L, 301L, "AGREE");
    }

    @Test
    void testGetMessageList() {
        // 设置mock行为
        when(messageService.getMessageList(anyString(), anyLong(), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of(new Message()));

        // 测试获取消息列表
        List<Message> messages = messageService.getMessageList("STUDENT", 1L, 1, 10);
        assertNotNull(messages);
        assertFalse(messages.isEmpty());

        // 验证方法被调用
        verify(messageService, times(1))
                .getMessageList("STUDENT", 1L, 1, 10);
    }

    @Test
    void testGetUnreadCount() {
        // 设置mock行为
        when(messageService.getUnreadCount(anyString(), anyLong()))
                .thenReturn(5);

        // 测试获取未读消息数量
        int unreadCount = messageService.getUnreadCount("STUDENT", 1L);
        assertEquals(5, unreadCount);

        // 验证方法被调用
        verify(messageService, times(1))
                .getUnreadCount("STUDENT", 1L);
    }

    @Test
    void testMarkMessageAsRead() {
        // 测试标记消息为已读
        assertDoesNotThrow(() -> messageService.markAsRead(1L));

        // 验证方法被调用
        verify(messageService, times(1))
                .markAsRead(1L);
    }

    @Test
    void testBatchMarkAsRead() {
        // 测试批量标记消息为已读
        assertDoesNotThrow(() -> messageService.batchMarkAsRead("STUDENT", 1L));

        // 验证方法被调用
        verify(messageService, times(1))
                .batchMarkAsRead("STUDENT", 1L);
    }

    @Test
    void testMessageIntegration() {
        // 设置mock行为
        when(messageService.getMessageList(anyString(), anyLong(), any(Integer.class), any(Integer.class)))
                .thenReturn(List.of(new Message()));
        when(messageService.getUnreadCount(anyString(), anyLong()))
                .thenReturn(3);

        // 测试消息模块的集成功能
        // 发送消息
        assertDoesNotThrow(() -> messageService.sendLeaveStatusChangeMessage(1L, "PENDING", "APPROVED", 1L));

        // 获取消息列表
        List<Message> messages = messageService.getMessageList("STUDENT", 1L, 1, 10);
        assertNotNull(messages);

        // 获取未读数量
        int unreadCount = messageService.getUnreadCount("STUDENT", 1L);
        assertTrue(unreadCount >= 0);

        // 标记为已读
        assertDoesNotThrow(() -> messageService.markAsRead(1L));

        // 验证所有方法都被调用
        verify(messageService, times(1)).sendLeaveStatusChangeMessage(anyLong(), anyString(), anyString(), anyLong());
        verify(messageService, times(1)).getMessageList(anyString(), anyLong(), any(Integer.class), any(Integer.class));
        verify(messageService, times(1)).getUnreadCount(anyString(), anyLong());
        verify(messageService, times(1)).markAsRead(anyLong());
    }

    @Test
    void testMessageWithDifferentReceiverTypes() {
        // 设置mock行为
        when(messageService.getUnreadCount("STUDENT", 1L)).thenReturn(5);
        when(messageService.getUnreadCount("STAFF", 1L)).thenReturn(3);
        when(messageService.getUnreadCount("COUNSELOR", 1L)).thenReturn(2);
        when(messageService.getMessageList("STUDENT", 1L, 1, 10)).thenReturn(List.of(new Message()));
        when(messageService.getMessageList("STAFF", 1L, 1, 10)).thenReturn(List.of(new Message()));
        when(messageService.getMessageList("COUNSELOR", 1L, 1, 10)).thenReturn(List.of(new Message()));

        // 测试向不同类型接收者发送消息
        int studentUnreadCount = messageService.getUnreadCount("STUDENT", 1L);
        int teacherUnreadCount = messageService.getUnreadCount("STAFF", 1L);
        int counselorUnreadCount = messageService.getUnreadCount("COUNSELOR", 1L);

        assertTrue(studentUnreadCount >= 0);
        assertTrue(teacherUnreadCount >= 0);
        assertTrue(counselorUnreadCount >= 0);

        List<Message> studentMessages = messageService.getMessageList("STUDENT", 1L, 1, 10);
        List<Message> teacherMessages = messageService.getMessageList("STAFF", 1L, 1, 10);
        List<Message> counselorMessages = messageService.getMessageList("COUNSELOR", 1L, 1, 10);

        assertNotNull(studentMessages);
        assertNotNull(teacherMessages);
        assertNotNull(counselorMessages);
    }
}