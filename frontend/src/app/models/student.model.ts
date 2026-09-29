export interface Student {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface StudentRequest {
  firstName: string;
  lastName: string;
  email: string;
}
