// src/api/messages.ts
import request from '@/utils/request';

export interface Message {
  messageId: number;
  receiverType?: string;
  receiverId?: number;
  senderType?: string;
  senderId?: number;
  messageType?: string;
  content?: string;
  relatedId?: number;
  isRead?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 获取当前登录用户的消息列表
 * GET /api/messages/list?page=1&size=10
 */
export const getMessageList = (params?: { page?: number; size?: number }) => {
  return request.get<Message[]>('/api/messages/list', { params });
};

/**
 * 获取当前登录用户未读消息数量
 * GET /api/messages/unread-count
 */
export const getUnreadCount = () => {
  return request.get<number>('/api/messages/unread-count');
};

/**
 * 标记某条消息为已读（只能标记自己的消息）
 * POST /api/messages/mark-as-read?messageId=1
 */
export const markAsRead = (messageId: number) => {
  return request.post<void>('/api/messages/mark-as-read', null, { params: { messageId } });
};

/**
 * 批量标记当前登录用户的消息为已读
 * POST /api/messages/batch-mark-as-read
 */
export const batchMarkAsRead = () => {
  return request.post<number>('/api/messages/batch-mark-as-read');
};
