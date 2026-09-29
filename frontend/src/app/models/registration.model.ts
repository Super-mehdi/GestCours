export type RegistrationStatus = 'ACTIVE' | 'INACTIVE';

export interface Registration {
  id?: number;
  studentId: number;
  courseId: number;
  status: RegistrationStatus;
}

export interface RegistrationRequest {
  studentId: number;
  courseId: number;
  registrationStatus?: RegistrationStatus;
}
