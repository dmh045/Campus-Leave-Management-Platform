// src/constants/statusMap.ts
import { LeaveStatus } from '@/types/leave';

interface StatusConfig {
  label: string;
  color: string; // 用于日历/课表背景色
  tagType?: 'success' | 'info' | 'warning' | 'danger' | ''; // Element Plus Tag 类型
}

export const LEAVE_STATUS_MAP: Record<LeaveStatus, StatusConfig> = {
  // --- 正常流程 ---
  [LeaveStatus.DRAFT]: { 
    label: '草稿', 
    color: '#909399', 
    tagType: 'info' 
  },
  [LeaveStatus.PENDING_COUNSELOR]: { 
    label: '待辅导员审核', 
    color: '#409EFF', 
    tagType: '' // 默认蓝色
  },
  [LeaveStatus.PENDING_TEACHER]: { 
    label: '待任课确认', 
    color: '#E6A23C', // 橙色
    tagType: 'warning' 
  },
  [LeaveStatus.EFFECTIVE]: { 
    label: '已生效', 
    color: '#E6A23C', // 黄色 (根据需求复用黄色系，或调整为偏亮黄)
    tagType: 'warning' 
  },
  
  // --- 异常/终态 ---
  [LeaveStatus.RETURN_SUPPLEMENT]: { 
    label: '退回补充', 
    color: '#B37FEB', // 紫色
    tagType: '' // 自定义颜色
  },
  [LeaveStatus.REJECTED]: { 
    label: '已驳回', 
    color: '#B37FEB', // 紫色
    tagType: 'danger' 
  },
  [LeaveStatus.CANCELLED]: { 
    label: '已取消', 
    color: '#909399', // 灰色
    tagType: 'info' 
  },
  [LeaveStatus.ENDED]: { 
    label: '已结束', 
    color: '#909399', // 灰色
    tagType: 'info' 
  },
  
  // --- 特殊状态 ---
  [LeaveStatus.PUBLIC_LEAVE]: { 
    label: '公假', 
    color: '#409EFF', // 蓝色
    tagType: '' 
  },
  [LeaveStatus.ABSENCE]: { 
    label: '缺勤/待补假', 
    color: '#F56C6C', // 红色
    tagType: 'danger' 
  },
  [LeaveStatus.PRESENT]: { 
    label: '到课', 
    color: '#67C23A', // 绿色
    tagType: 'success' 
  },
};

/**
 * 辅助函数：获取状态配置，避免 undefined 报错
 */
export const getStatusConfig = (status: LeaveStatus): StatusConfig => {
  return LEAVE_STATUS_MAP[status] || { label: '未知状态', color: '#000', tagType: 'info' };
};