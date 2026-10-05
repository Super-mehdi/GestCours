export type RegistrationStatus = 'PENDING' | 'ACTIVE' | 'INACTIVE' | 'REJECTED';

export interface Registration {
  id?: number;
  studentId: number;
  courseId: number;
  registrationStatus?: RegistrationStatus;
  status?: RegistrationStatus;
}

export interface RegistrationRequest {
  studentId: number;
  courseId: number;
  registrationStatus?: RegistrationStatus;
}

export interface RegistrationDetail {
  id: number;
  studentId: number;
  studentName: string;
  studentEmail: string;
  courseId: number;
  courseCode: string;
  courseName: string;
  status: RegistrationStatus;
}
