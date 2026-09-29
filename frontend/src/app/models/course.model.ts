export interface Course {
  id?: number;
  name: string;
  code: string;
  description: string;
  capacity: number;
}

export interface CourseRequest {
  name: string;
  code: string;
  description: string;
  capacity: number;
}
