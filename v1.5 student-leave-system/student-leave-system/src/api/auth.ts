// src/api/auth.ts
import request from '@/utils/request';

export type LoginType = 'STUDENT' | 'STAFF';

export interface LoginRequest {
  username: string;
  password: string;
  loginType: LoginType;
}

export interface LoginResponse {
  userId: number;
  displayName: string;
  userType: string;
  token: string;
  roleCode: string;
}

export const login = (payload: LoginRequest) => {
  return request.post<LoginResponse>('/api/auth/login', payload);
};

// 后端是 POST /api/auth/logout?token=xxx
export const logout = (token: string) => {
  return request.post<void>('/api/auth/logout', null, { params: { token } });
};
