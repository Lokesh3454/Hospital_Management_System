import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { Prescription, PrescriptionRequest } from '../../models/prescription.model';

@Injectable({
  providedIn: 'root'
})
export class PrescriptionService {
  private apiUrl = `${environment.apiUrl}/prescriptions`;

  constructor(private http: HttpClient) {}

  getAll(filters?: {
    search?: string;
    date?: string;
    patientId?: number;
    doctorId?: number;
  }): Observable<ApiResponse<Prescription[]>> {
    let params = new HttpParams();
    if (filters?.search && filters.search.trim()) {
      params = params.set('search', filters.search.trim());
    }
    if (filters?.date) {
      params = params.set('date', filters.date);
    }
    if (filters?.patientId) {
      params = params.set('patientId', filters.patientId.toString());
    }
    if (filters?.doctorId) {
      params = params.set('doctorId', filters.doctorId.toString());
    }

    return this.http.get<ApiResponse<Prescription[]>>(this.apiUrl, { params });
  }

  getById(id: number): Observable<ApiResponse<Prescription>> {
    return this.http.get<ApiResponse<Prescription>>(`${this.apiUrl}/${id}`);
  }

  getPatientPrescriptions(patientId: number): Observable<ApiResponse<Prescription[]>> {
    return this.http.get<ApiResponse<Prescription[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  getDoctorPrescriptions(doctorId: number): Observable<ApiResponse<Prescription[]>> {
    return this.http.get<ApiResponse<Prescription[]>>(`${this.apiUrl}/doctor/${doctorId}`);
  }

  getByAppointment(appointmentId: number): Observable<ApiResponse<Prescription[]>> {
    return this.http.get<ApiResponse<Prescription[]>>(`${this.apiUrl}/appointment/${appointmentId}`);
  }

  create(prescription: PrescriptionRequest): Observable<ApiResponse<Prescription>> {
    return this.http.post<ApiResponse<Prescription>>(this.apiUrl, prescription);
  }

  update(id: number, prescription: PrescriptionRequest): Observable<ApiResponse<Prescription>> {
    return this.http.put<ApiResponse<Prescription>>(`${this.apiUrl}/${id}`, prescription);
  }
}
