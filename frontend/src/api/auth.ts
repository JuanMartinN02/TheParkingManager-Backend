import { api, forgetCsrfToken, request } from './client';
import type { components } from './schema';

export type Me = components['schemas']['UserResponse'];
export type Role = NonNullable<Me['role']>;

// Spring's built in login reads form fields, not JSON
export async function login(email: string, password: string): Promise<void> {
  await request<void>('POST', '/auth/login', { form: new URLSearchParams({ email, password }), isLogin: true });
  forgetCsrfToken();
}

export async function logout(): Promise<void> {
  await api.post<void>('/auth/logout');
  forgetCsrfToken();
}

export function getMe(): Promise<Me> {
  return api.get<Me>('/auth/me');
}

export function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  return api.post<void>('/auth/change-password', { currentPassword, newPassword });
}