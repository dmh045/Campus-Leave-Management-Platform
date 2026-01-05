package com.example.leavesystem;

import com.example.leavesystem.entity.Message;
import com.example.leavesystem.mapper.MessageMapper;
import com.example.leavesystem.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageModuleTest {

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    void sendLeaveStatusChangeMessage_shouldInsertCorrectMessage() {
        // when
        messageService.sendLeaveStatusChangeMessage(1L, "PENDING", "APPROVED", 1001L);

        // then
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageMapper, times(1)).insertMessage(captor.capture());
        verifyNoMoreInteractions(messageMapper);

        Message msg = captor.getValue();
        assertNotNull(msg);

        assertEquals("STUDENT", msg.getReceiverType());
        assertEquals(1001L, msg.getReceiverId());
        assertEquals("SYSTEM", msg.getSenderType());
        assertEquals("LEAVE_STATUS_CHANGE", msg.getMessageType());

        assertNotNull(msg.getContent());
        assertTrue(msg.getContent().contains("ID:1"));
        assertTrue(msg.getContent().contains("PENDING"));
        assertTrue(msg.getContent().contains("APPROVED"));

        assertEquals(1L, msg.getRelatedId());
        assertFalse(msg.getIsRead());
        assertNotNull(msg.getCreatedAt());
    }

    @Test
    void sendTeacherConfirmMessage_shouldInsertCorrectMessage() {
        // when
        messageService.sendTeacherConfirmMessage(2L, 2002L, 3003L, "已确认");

        // then
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageMapper, times(1)).insertMessage(captor.capture());
        verifyNoMoreInteractions(messageMapper);

        Message msg = captor.getValue();
        assertNotNull(msg);

        assertEquals("STUDENT", msg.getReceiverType());
        assertEquals(2002L, msg.getReceiverId());
        assertEquals("STAFF", msg.getSenderType());
        assertEquals(3003L, msg.getSenderId());
        assertEquals("TEACHER_CONFIRM", msg.getMessageType());

        assertNotNull(msg.getContent());
        assertTrue(msg.getContent().contains("ID:2"));
        assertTrue(msg.getContent().contains("已确认"));

        assertEquals(2L, msg.getRelatedId());
        assertFalse(msg.getIsRead());
        assertNotNull(msg.getCreatedAt());
    }

    @Test
    void sendCounselorApproveMessage_shouldInsertCorrectMessage_forAgree() {
        // when
        messageService.sendCounselorApproveMessage(3L, 111L, 222L, "AGREE");

        // then
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageMapper, times(1)).insertMessage(captor.capture());
        verifyNoMoreInteractions(messageMapper);

        Message msg = captor.getValue();
        assertNotNull(msg);

        // receiver / sender
        assertEquals("STUDENT", msg.getReceiverType());
        assertEquals(111L, msg.getReceiverId());
        assertEquals("STAFF", msg.getSenderType());
        assertEquals(222L, msg.getSenderId());

        // message fields
        assertEquals("COUNSELOR_APPROVE", msg.getMessageType());
        assertEquals(3L, msg.getRelatedId());
        assertFalse(msg.getIsRead());
        assertNotNull(msg.getCreatedAt());

        assertNotNull(msg.getContent());
        assertTrue(msg.getContent().contains("批准"));
        assertTrue(msg.getContent().contains("ID:3"));
    }

    @Test
    void sendCounselorApproveMessage_shouldInsertCorrectMessage_forReject() {
        // when
        messageService.sendCounselorApproveMessage(4L, 111L, 222L, "REJECT");

        // then
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageMapper, times(1)).insertMessage(captor.capture());
        verifyNoMoreInteractions(messageMapper);

        Message msg = captor.getValue();
        assertNotNull(msg);

        // receiver / sender
        assertEquals("STUDENT", msg.getReceiverType());
        assertEquals(111L, msg.getReceiverId());
        assertEquals("STAFF", msg.getSenderType());
        assertEquals(222L, msg.getSenderId());

        // message fields
        assertEquals("COUNSELOR_APPROVE", msg.getMessageType());
        assertEquals(4L, msg.getRelatedId());
        assertFalse(msg.getIsRead());
        assertNotNull(msg.getCreatedAt());

        assertNotNull(msg.getContent());
        assertTrue(msg.getContent().contains("拒绝"));
        assertTrue(msg.getContent().contains("ID:4"));
    }

    @Test
    void getMessageList_page1_size10_shouldUseOffset0() {
        // offset=0, size=10（注意顺序）
        when(messageMapper.selectMessagesByReceiverWithPaging(eq("STUDENT"), eq(1L), eq(0), eq(10)))
                .thenReturn(Collections.emptyList());

        List<Message> list = messageService.getMessageList("STUDENT", 1L, 1, 10);

        assertNotNull(list);
        assertEquals(0, list.size());

        verify(messageMapper, times(1))
                .selectMessagesByReceiverWithPaging("STUDENT", 1L, 0, 10);
        verifyNoMoreInteractions(messageMapper);
    }

    @Test
    void getMessageList_page2_size10_shouldUseOffset10() {
        // page=2,size=10 => offset=10,size=10（注意顺序）
        when(messageMapper.selectMessagesByReceiverWithPaging(eq("STUDENT"), eq(1L), eq(10), eq(10)))
                .thenReturn(List.of(new Message()));

        List<Message> list = messageService.getMessageList("STUDENT", 1L, 2, 10);

        assertNotNull(list);
        assertEquals(1, list.size());

        verify(messageMapper, times(1))
                .selectMessagesByReceiverWithPaging("STUDENT", 1L, 10, 10);
        verifyNoMoreInteractions(messageMapper);
    }


    @Test
    void getUnreadCount_shouldReturnMapperValue() {
        when(messageMapper.selectUnreadCount("STUDENT", 1L)).thenReturn(5);

        int count = messageService.getUnreadCount("STUDENT", 1L);

        assertEquals(5, count);
        verify(messageMapper, times(1)).selectUnreadCount("STUDENT", 1L);
        verifyNoMoreInteractions(messageMapper);
    }

    @Test
    void markAsRead_shouldNotThrow_whenUpdated() {
        when(messageMapper.updateMessageAsReadForReceiver(1L, "STUDENT", 1L)).thenReturn(1);

        assertDoesNotThrow(() -> messageService.markAsRead(1L, "STUDENT", 1L));

        verify(messageMapper, times(1)).updateMessageAsReadForReceiver(1L, "STUDENT", 1L);
        verifyNoMoreInteractions(messageMapper);
    }

    @Test
    void markAsRead_shouldThrow_whenNotOwnedOrNotExist() {
        when(messageMapper.updateMessageAsReadForReceiver(1L, "STUDENT", 1L)).thenReturn(0);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> messageService.markAsRead(1L, "STUDENT", 1L));

        // 兼容你现在实现：可能是“无权限”也可能是“不存在”
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("无权限") || ex.getMessage().contains("不存在"));

        verify(messageMapper, times(1)).updateMessageAsReadForReceiver(1L, "STUDENT", 1L);
        verifyNoMoreInteractions(messageMapper);
    }

    @Test
    void batchMarkAsRead_shouldReturnUpdatedRows() {
        when(messageMapper.updateMessagesAsRead("STUDENT", 1L)).thenReturn(7);

        int updated = messageService.batchMarkAsRead("STUDENT", 1L);

        assertEquals(7, updated);
        verify(messageMapper, times(1)).updateMessagesAsRead("STUDENT", 1L);
        verifyNoMoreInteractions(messageMapper);
    }
}
