// src/config/menu.ts
import { 
  Calendar, FileText, ClipboardCheck, Users, 
  BarChart3, CheckCircle,  BookOpen, Settings,
  MapPin,Download
} from 'lucide-vue-next';

// 这里定义了不同角色的菜单，把新功能都加进去了
export const roleMenus = {
  // 🎓 学生菜单
  student: [
    { label: '我的课表', path: '/student/timetable', icon: Calendar },
    { label: '请假记录', path: '/leave/list', icon: FileText },
    { label: '发起请假', path: '/leave/apply', icon: FileText },
    { label: '课堂签到', path: '/student/checkin', icon: MapPin }, // 👈 新增入口
  ],
  
  // 📋 辅导员菜单
  counselor: [
    { label: '待办审批', path: '/tutor/todos', icon: ClipboardCheck },
    { label: '发起公假', path: '/counselor/public-leave', icon: Users }, // 👈 新增入口
    { label: '班级统计', path: '/counselor/statistics', icon: BarChart3 }, // 👈 新增入口
  ],
  
  // 👨‍🏫 任课老师菜单
  teacher: [
    { label: '请假确认', path: '/teacher/confirm', icon: CheckCircle },
    { label: '考勤登记', path: '/teacher/attendance', icon: ClipboardCheck },
    { label: '导出考勤', path: '/teacher/export', icon: Download },// 👈 新增入口
  ],
  
  // ⚙️ 管理员菜单
  admin: [
    { label: '课表管理', path: '/admin/timetable', icon: BookOpen },
    { label: '系统设置', path: '/admin/settings', icon: Settings },
  ]
};