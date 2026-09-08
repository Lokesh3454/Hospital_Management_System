import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { LabTest } from '../../models/lab-test.model';

@Injectable({
  providedIn: 'root'
})
export class LabTestService {
  private apiUrl = `${environment.apiUrl}/lab-tests`;

  constructor(private http: HttpClient) {}

  getAllLabTests(): Observable<ApiResponse<LabTest[]>> {
    return this.http.get<ApiResponse<LabTest[]>>(this.apiUrl);
  }

  getLabTestById(id: number): Observable<ApiResponse<LabTest>> {
    return this.http.get<ApiResponse<LabTest>>(`${this.apiUrl}/${id}`);
  }

  getLabTestsByPatient(patientId: number): Observable<ApiResponse<LabTest[]>> {
    return this.http.get<ApiResponse<LabTest[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  getLabTestsByDoctor(doctorId: number): Observable<ApiResponse<LabTest[]>> {
    return this.http.get<ApiResponse<LabTest[]>>(`${this.apiUrl}/doctor/${doctorId}`);
  }

  createLabTest(test: LabTest): Observable<ApiResponse<LabTest>> {
    return this.http.post<ApiResponse<LabTest>>(this.apiUrl, test);
  }

  updateStatus(id: number, status: string): Observable<ApiResponse<LabTest>> {
    return this.http.put<ApiResponse<LabTest>>(`${this.apiUrl}/${id}/status`, { status });
  }

  recordResults(id: number, resultValue: string, interpretation: string, remarks?: string): Observable<ApiResponse<LabTest>> {
    return this.http.post<ApiResponse<LabTest>>(`${this.apiUrl}/${id}/results`, { resultValue, interpretation, remarks });
  }

  deleteLabTest(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
