package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.entity.Message;
import com.example.leavesystem.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取消息列表
     *
     * GET /api/messages/list?receiverId=1&receiverType=STUDENT&page=1&size=10
     */
    @GetMapping("/list")
    public Result<List<Message>> getMessageList(
            @RequestParam Long receiverId,
            @RequestParam String receiverType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        List<Message> messages = messageService.getMessageList(receiverType, receiverId, page, size);
        return Result.success(messages);
    }

    /**
     * 获取未读消息数量
     *
     * GET /api/messages/unread-count?receiverId=1&receiverType=STUDENT
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(
            @RequestParam Long receiverId,
            @RequestParam String receiverType) {

        int unreadCount = messageService.getUnreadCount(receiverType, receiverId);
        return Result.success(unreadCount);
    }

    /**
     * 标记消息为已读
     *
     * POST /api/messages/mark-as-read?messageId=1
     */
    @PostMapping("/mark-as-read")
    public Result<Void> markAsRead(@RequestParam Long messageId) {
        messageService.markAsRead(messageId);
        return Result.success(null);
    }

    /**
     * 批量标记消息为已读
     *
     * POST /api/messages/batch-mark-as-read?receiverType=STUDENT&receiverId=1
     */
    @PostMapping("/batch-mark-as-read")
    public Result<Void> batchMarkAsRead(
            @RequestParam String receiverType,
            @RequestParam Long receiverId) {

        messageService.batchMarkAsRead(receiverType, receiverId);
        return Result.success(null);
    }
}