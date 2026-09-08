import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { SurgerySchedule } from '../../models/surgery.model';

@Injectable({
  providedIn: 'root'
})
export class SurgeryService {
  private apiUrl = `${environment.apiUrl}/surgery`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<SurgerySchedule[]>> {
    return this.http.get<ApiResponse<SurgerySchedule[]>>(this.apiUrl);
  }

  getByDate(date: string): Observable<ApiResponse<SurgerySchedule[]>> {
    return this.http.get<ApiResponse<SurgerySchedule[]>>(`${this.apiUrl}/date/${date}`);
  }

  getByRoom(otRoom: string): Observable<ApiResponse<SurgerySchedule[]>> {
    return this.http.get<ApiResponse<SurgerySchedule[]>>(`${this.apiUrl}/room/${otRoom}`);
  }

  getById(id: number): Observable<ApiResponse<SurgerySchedule>> {
    return this.http.get<ApiResponse<SurgerySchedule>>(`${this.apiUrl}/${id}`);
  }

  scheduleSurgery(surgery: SurgerySchedule): Observable<ApiResponse<SurgerySchedule>> {
    return this.http.post<ApiResponse<SurgerySchedule>>(this.apiUrl, surgery);
  }

  updateStatus(id: number, status: string, pacuRecoveryScore?: number): Observable<ApiResponse<SurgerySchedule>> {
    let params = new HttpParams().set('status', status);
    if (pacuRecoveryScore !== undefined && pacuRecoveryScore !== null) {
      params = params.set('pacuRecoveryScore', pacuRecoveryScore.toString());
    }
    return this.http.put<ApiResponse<SurgerySchedule>>(`${this.apiUrl}/${id}/status`, null, { params });
  }

  updateClearance(id: number, clearance: {
    preOpCleared?: boolean;
    anesthesiaCleared?: boolean;
    consentSigned?: boolean;
    bloodReserved?: boolean;
  }): Observable<ApiResponse<SurgerySchedule>> {
    let params = new HttpParams();
    if (clearance.preOpCleared !== undefined) params = params.set('preOpCleared', clearance.preOpCleared.toString());
    if (clearance.anesthesiaCleared !== undefined) params = params.set('anesthesiaCleared', clearance.anesthesiaCleared.toString());
    if (clearance.consentSigned !== undefined) params = params.set('consentSigned', clearance.consentSigned.toString());
    if (clearance.bloodReserved !== undefined) params = params.set('bloodReserved', clearance.bloodReserved.toString());
    return this.http.put<ApiResponse<SurgerySchedule>>(`${this.apiUrl}/${id}/clearance`, null, { params });
  }
}
