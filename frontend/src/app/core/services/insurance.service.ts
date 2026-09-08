import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { InsuranceClaim } from '../../models/insurance.model';

@Injectable({
  providedIn: 'root'
})
export class InsuranceService {
  private apiUrl = `${environment.apiUrl}/insurance`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<InsuranceClaim[]>> {
    return this.http.get<ApiResponse<InsuranceClaim[]>>(this.apiUrl);
  }

  getByPatientId(patientId: number): Observable<ApiResponse<InsuranceClaim[]>> {
    return this.http.get<ApiResponse<InsuranceClaim[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  getByStatus(status: string): Observable<ApiResponse<InsuranceClaim[]>> {
    return this.http.get<ApiResponse<InsuranceClaim[]>>(`${this.apiUrl}/status/${status}`);
  }

  getById(id: number): Observable<ApiResponse<InsuranceClaim>> {
    return this.http.get<ApiResponse<InsuranceClaim>>(`${this.apiUrl}/${id}`);
  }

  submitClaim(claim: InsuranceClaim): Observable<ApiResponse<InsuranceClaim>> {
    return this.http.post<ApiResponse<InsuranceClaim>>(this.apiUrl, claim);
  }

  updatePreAuth(id: number, status: string, approvedAmount?: number, notes?: string): Observable<ApiResponse<InsuranceClaim>> {
    let params = new HttpParams().set('status', status);
    if (approvedAmount !== undefined && approvedAmount !== null) {
      params = params.set('approvedAmount', approvedAmount.toString());
    }
    if (notes) {
      params = params.set('notes', notes);
    }
    return this.http.put<ApiResponse<InsuranceClaim>>(`${this.apiUrl}/${id}/pre-auth`, null, { params });
  }
}
