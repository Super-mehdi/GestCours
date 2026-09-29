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
}
