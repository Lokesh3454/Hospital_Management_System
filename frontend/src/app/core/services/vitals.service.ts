import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { Vitals } from '../../models/vitals.model';

@Injectable({
  providedIn: 'root'
})
export class VitalsService {
  private apiUrl = `${environment.apiUrl}/vitals`;

  constructor(private http: HttpClient) {}

  recordVitals(vitals: Vitals): Observable<ApiResponse<Vitals>> {
    return this.http.post<ApiResponse<Vitals>>(this.apiUrl, vitals);
  }

  getPatientVitals(patientId: number): Observable<ApiResponse<Vitals[]>> {
    return this.http.get<ApiResponse<Vitals[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  getLatestVitals(patientId: number): Observable<ApiResponse<Vitals>> {
    return this.http.get<ApiResponse<Vitals>>(`${this.apiUrl}/patient/${patientId}/latest`);
  }

  deleteVitals(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
