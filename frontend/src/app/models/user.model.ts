export type UserRole = 'ROLE_ADMIN' | 'ROLE_STUDENT';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  role: UserRole;
  studentId: number | null;
  studentName: string;
}

export interface TestCredential {
  label: string;
  email: string;
  password: string;
  role: string;
  description: string;
}
