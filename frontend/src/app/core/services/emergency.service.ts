import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { EmergencyCase } from '../../models/emergency.model';

@Injectable({
  providedIn: 'root'
})
export class EmergencyService {
  private apiUrl = `${environment.apiUrl}/emergency`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<EmergencyCase[]>> {
    return this.http.get<ApiResponse<EmergencyCase[]>>(this.apiUrl);
  }

  getActive(): Observable<ApiResponse<EmergencyCase[]>> {
    return this.http.get<ApiResponse<EmergencyCase[]>>(`${this.apiUrl}/active`);
  }

  getById(id: number): Observable<ApiResponse<EmergencyCase>> {
    return this.http.get<ApiResponse<EmergencyCase>>(`${this.apiUrl}/${id}`);
  }

  createCase(caseData: EmergencyCase): Observable<ApiResponse<EmergencyCase>> {
    return this.http.post<ApiResponse<EmergencyCase>>(this.apiUrl, caseData);
  }

  updateTriage(id: number, triageLevel: string, notes?: string): Observable<ApiResponse<EmergencyCase>> {
    return this.http.put<ApiResponse<EmergencyCase>>(`${this.apiUrl}/${id}/triage`, { triageLevel, notes });
  }

  updateStatus(id: number, status: string, assignedBedNumber?: string): Observable<ApiResponse<EmergencyCase>> {
    return this.http.put<ApiResponse<EmergencyCase>>(`${this.apiUrl}/${id}/status`, { status, assignedBedNumber });
  }

  triggerCodeBlue(id: number): Observable<ApiResponse<EmergencyCase>> {
    return this.http.post<ApiResponse<EmergencyCase>>(`${this.apiUrl}/${id}/code-blue`, {});
  }
}
