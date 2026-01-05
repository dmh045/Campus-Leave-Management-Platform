package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.entity.Message;
import com.example.leavesystem.security.AuthContext;
import com.example.leavesystem.security.RequiresRoles;
import com.example.leavesystem.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@RequiresRoles // MOD: 至少要求登录
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取当前登录用户的消息列表
     *
     * GET /api/messages/list?page=1&size=10
     */
    @GetMapping("/list")
    public Result<List<Message>> getMessageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        String receiverType = AuthContext.getCurrentRole();     // MOD: 从登录态取
        Long receiverId = AuthContext.getCurrentUserId();       // MOD: 从登录态取

        List<Message> messages = messageService.getMessageList(receiverType, receiverId, page, size);
        return Result.success(messages);
    }

    /**
     * 获取当前登录用户的未读消息数量
     *
     * GET /api/messages/unread-count
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {

        String receiverType = AuthContext.getCurrentRole(); // MOD
        Long receiverId = AuthContext.getCurrentUserId();   // MOD

        int unreadCount = messageService.getUnreadCount(receiverType, receiverId);
        return Result.success(unreadCount);
    }

    /**
     * 标记某条消息为已读（只能标记“自己的消息”）
     *
     * POST /api/messages/mark-as-read?messageId=1
     */
    @PostMapping("/mark-as-read")
    public Result<Void> markAsRead(@RequestParam Long messageId) {

        String receiverType = AuthContext.getCurrentRole(); // MOD
        Long receiverId = AuthContext.getCurrentUserId();   // MOD

        // MOD: service 层必须校验 messageId 是否属于当前 receiver
        messageService.markAsRead(messageId, receiverType, receiverId);

        return Result.success(null);
    }

    /**
     * 批量标记当前登录用户的消息为已读
     *
     * POST /api/messages/batch-mark-as-read
     */
    @PostMapping("/batch-mark-as-read")
    public Result<Integer> batchMarkAsRead() {

        String receiverType = AuthContext.getCurrentRole(); // MOD
        Long receiverId = AuthContext.getCurrentUserId();   // MOD

        int updated = messageService.batchMarkAsRead(receiverType, receiverId); // MOD: 返回更新条数
        return Result.success(updated);
    }
}
