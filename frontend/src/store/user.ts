// src/store/user.ts
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { login as apiLogin, logout as apiLogout, type LoginRequest, type LoginResponse } from '@/api/auth';

export type UserRole = 'student' | 'counselor' | 'teacher' | 'admin';

function mapRoleFromRoleCode(roleCode?: string, userType?: string): UserRole {
  const rc = (roleCode || '').toUpperCase();
  if (rc === 'STUDENT') return 'student';
  if (rc === 'COUNSELOR') return 'counselor';
  if (rc === 'TEACHER') return 'teacher';
  if (rc === 'ADMIN') return 'admin';

  // 兜底：STAFF 未知角色按 teacher 视角展示；否则按 student
  return (userType || '').toUpperCase() === 'STAFF' ? 'teacher' : 'student';
}

function avatarColorByRole(role: UserRole): string {
  switch (role) {
    case 'student': return 'bg-blue-500';
    case 'counselor': return 'bg-emerald-500';
    case 'teacher': return 'bg-purple-500';
    case 'admin': return 'bg-orange-500';
    default: return 'bg-gray-500';
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '');
  const userId = ref<number | null>(localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null);
  const displayName = ref(localStorage.getItem('displayName') || '');
  const userType = ref(localStorage.getItem('userType') || '');
  const roleCode = ref(localStorage.getItem('roleCode') || '');

  const currentRole = computed<UserRole>(() => mapRoleFromRoleCode(roleCode.value, userType.value));

  const userInfo = computed(() => ({
    name: displayName.value || '未登录',
    avatarColor: avatarColorByRole(currentRole.value)
  }));

  function applyLogin(resp: LoginResponse) {
    token.value = resp.token || '';
    userId.value = typeof resp.userId === 'number' ? resp.userId : Number(resp.userId);
    displayName.value = resp.displayName || '';
    userType.value = resp.userType || '';
    roleCode.value = resp.roleCode || '';

    localStorage.setItem('token', token.value);
    localStorage.setItem('userId', String(userId.value ?? ''));
    localStorage.setItem('displayName', displayName.value);
    localStorage.setItem('userType', userType.value);
    localStorage.setItem('roleCode', roleCode.value);

    // ✅ 兼容旧代码（比如 course.ts 里用 demo_role 判断是否学生）
    localStorage.setItem('demo_role', currentRole.value);
  }

  function clearAuth() {
    token.value = '';
    userId.value = null;
    displayName.value = '';
    userType.value = '';
    roleCode.value = '';

    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('displayName');
    localStorage.removeItem('userType');
    localStorage.removeItem('roleCode');
    localStorage.removeItem('demo_role');
  }

  async function login(payload: LoginRequest): Promise<LoginResponse> {
    const resp = await apiLogin(payload);
    applyLogin(resp);
    return resp;
  }

  async function logout() {
    const t = token.value;
    clearAuth();
    if (!t) return;

    try {
      await apiLogout(t);
    } catch {
      // 后端登出失败不影响前端清理
    }
    ElMessage.success('已退出登录');
  }

  const isAuthed = computed(() => !!token.value);

  return {
    token,
    userId,
    displayName,
    userType,
    roleCode,
    currentRole,
    userInfo,
    isAuthed,
    applyLogin,
    clearAuth,
    login,
    logout
  };
});
