import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import {
  Appointment,
  AppointmentRequest,
  RescheduleRequest,
  TimeSlot
} from '../../models/appointment.model';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private apiUrl = `${environment.apiUrl}/appointments`;

  constructor(private http: HttpClient) {}

  getAll(filters?: {
    search?: string;
    status?: string;
    date?: string;
    doctorId?: number;
    patientId?: number;
  }): Observable<ApiResponse<Appointment[]>> {
    let params = new HttpParams();
    if (filters?.search && filters.search.trim()) {
      params = params.set('search', filters.search.trim());
    }
    if (filters?.status && filters.status.trim()) {
      params = params.set('status', filters.status.trim());
    }
    if (filters?.date) {
      params = params.set('date', filters.date);
    }
    if (filters?.doctorId) {
      params = params.set('doctorId', filters.doctorId.toString());
    }
    if (filters?.patientId) {
      params = params.set('patientId', filters.patientId.toString());
    }

    return this.http.get<ApiResponse<Appointment[]>>(this.apiUrl, { params });
  }

  getById(id: number): Observable<ApiResponse<Appointment>> {
    return this.http.get<ApiResponse<Appointment>>(`${this.apiUrl}/${id}`);
  }

  getByPatient(patientId: number): Observable<ApiResponse<Appointment[]>> {
    return this.http.get<ApiResponse<Appointment[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  getByDoctor(doctorId: number): Observable<ApiResponse<Appointment[]>> {
    return this.http.get<ApiResponse<Appointment[]>>(`${this.apiUrl}/doctor/${doctorId}`);
  }

  getAvailableSlots(doctorId: number, date: string): Observable<ApiResponse<TimeSlot[]>> {
    const params = new HttpParams()
      .set('doctorId', doctorId.toString())
      .set('date', date);
    return this.http.get<ApiResponse<TimeSlot[]>>(`${this.apiUrl}/available-slots`, { params });
  }

  book(request: AppointmentRequest): Observable<ApiResponse<Appointment>> {
    return this.http.post<ApiResponse<Appointment>>(this.apiUrl, request);
  }

  reschedule(id: number, request: RescheduleRequest): Observable<ApiResponse<Appointment>> {
    return this.http.put<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/reschedule`, request);
  }

  cancel(id: number, reason?: string): Observable<ApiResponse<Appointment>> {
    return this.http.put<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/cancel`, { reason: reason || '' });
  }

  confirm(id: number): Observable<ApiResponse<Appointment>> {
    return this.http.put<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/confirm`, {});
  }

  complete(id: number): Observable<ApiResponse<Appointment>> {
    return this.http.put<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/complete`, {});
  }
}
