import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Registration, RegistrationRequest } from '../models/registration.model';

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {
  private readonly apiUrl = 'http://localhost:8082/api/registrations';

  constructor(private http: HttpClient) {}

  getAllRegistrations(): Observable<Registration[]> {
    return this.http.get<Registration[]>(this.apiUrl);
  }

  getRegistrationById(id: number): Observable<Registration> {
    return this.http.get<Registration>(`${this.apiUrl}/${id}`);
  }

  getRegistrationsByStudentId(studentId: number): Observable<Registration[]> {
    return this.http.get<Registration[]>(`${this.apiUrl}/student/${studentId}`);
  }

  createRegistration(request: RegistrationRequest): Observable<Registration> {
    return this.http.post<Registration>(this.apiUrl, request);
  }

  deleteRegistration(id: number): Observable<Registration> {
    return this.http.delete<Registration>(`${this.apiUrl}/${id}`);
  }

  requestCourseEnrollment(studentId: number, courseId: number): Observable<Registration> {
    return this.http.post<Registration>(`${this.apiUrl}/request?studentId=${studentId}&courseId=${courseId}`, {});
  }

  getPendingRegistrations(): Observable<import('../models/registration.model').RegistrationDetail[]> {
    return this.http.get<import('../models/registration.model').RegistrationDetail[]>(`${this.apiUrl}/pending`);
  }

  approveRegistration(id: number): Observable<Registration> {
    return this.http.put<Registration>(`${this.apiUrl}/${id}/approve`, {});
  }

  rejectRegistration(id: number): Observable<Registration> {
    return this.http.put<Registration>(`${this.apiUrl}/${id}/reject`, {});
  }
}
