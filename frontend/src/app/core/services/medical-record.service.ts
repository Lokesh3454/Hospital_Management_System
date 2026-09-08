import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { MedicalRecord, MedicalRecordRequest } from '../../models/medical-record.model';

@Injectable({
  providedIn: 'root'
})
export class MedicalRecordService {
  private apiUrl = `${environment.apiUrl}/medical-records`;

  constructor(private http: HttpClient) {}

  getAll(filters?: {
    search?: string;
    date?: string;
    patientId?: number;
    doctorId?: number;
  }): Observable<ApiResponse<MedicalRecord[]>> {
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

    return this.http.get<ApiResponse<MedicalRecord[]>>(this.apiUrl, { params });
  }

  getById(id: number): Observable<ApiResponse<MedicalRecord>> {
    return this.http.get<ApiResponse<MedicalRecord>>(`${this.apiUrl}/${id}`);
  }

  getPatientHistory(patientId: number): Observable<ApiResponse<MedicalRecord[]>> {
    return this.http.get<ApiResponse<MedicalRecord[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  getDoctorRecords(doctorId: number): Observable<ApiResponse<MedicalRecord[]>> {
    return this.http.get<ApiResponse<MedicalRecord[]>>(`${this.apiUrl}/doctor/${doctorId}`);
  }

  create(record: MedicalRecordRequest): Observable<ApiResponse<MedicalRecord>> {
    return this.http.post<ApiResponse<MedicalRecord>>(this.apiUrl, record);
  }

  update(id: number, record: MedicalRecordRequest): Observable<ApiResponse<MedicalRecord>> {
    return this.http.put<ApiResponse<MedicalRecord>>(`${this.apiUrl}/${id}`, record);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  clearAllHistory(): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/all`);
  }

  seedSampleData(): Observable<ApiResponse<MedicalRecord[]>> {
    return this.http.post<ApiResponse<MedicalRecord[]>>(`${this.apiUrl}/seed-sample-data`, {});
  }
}
