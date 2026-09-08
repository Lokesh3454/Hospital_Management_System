import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { SafetyCheckRequest, SafetyAlert, ClinicalBrief } from '../../models/clinical-support.model';

@Injectable({
  providedIn: 'root'
})
export class ClinicalSupportService {
  private apiUrl = `${environment.apiUrl}/clinical-support`;

  constructor(private http: HttpClient) {}

  checkSafety(request: SafetyCheckRequest): Observable<ApiResponse<SafetyAlert[]>> {
    return this.http.post<ApiResponse<SafetyAlert[]>>(`${this.apiUrl}/check-safety`, request);
  }

  getPatientBrief(patientId: number): Observable<ApiResponse<ClinicalBrief>> {
    return this.http.get<ApiResponse<ClinicalBrief>>(`${this.apiUrl}/patient-brief/${patientId}`);
  }
}
