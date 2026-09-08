import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Patient } from '../../models/patient.model';
import { ApiResponse } from '../../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class PatientService {
  private apiUrl = `${environment.apiUrl}/patients`;

  constructor(private http: HttpClient) {}

  getAll(search?: string, gender?: string, bloodGroup?: string, name?: string, phone?: string, email?: string, doctorId?: number): Observable<ApiResponse<Patient[]>> {
    let params = new HttpParams();
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }
    if (name && name.trim()) {
      params = params.set('name', name.trim());
    }
    if (phone && phone.trim()) {
      params = params.set('phone', phone.trim());
    }
    if (email && email.trim()) {
      params = params.set('email', email.trim());
    }
    if (gender && gender.trim()) {
      params = params.set('gender', gender.trim());
    }
    if (bloodGroup && bloodGroup.trim()) {
      params = params.set('bloodGroup', bloodGroup.trim());
    }
    if (doctorId) {
      params = params.set('doctorId', doctorId.toString());
    }

    return this.http.get<ApiResponse<Patient[]>>(this.apiUrl, { params });
  }

  getById(id: number): Observable<ApiResponse<Patient>> {
    return this.http.get<ApiResponse<Patient>>(`${this.apiUrl}/${id}`);
  }

  create(patient: Patient): Observable<ApiResponse<Patient>> {
    return this.http.post<ApiResponse<Patient>>(this.apiUrl, patient);
  }

  update(id: number, patient: Patient): Observable<ApiResponse<Patient>> {
    return this.http.put<ApiResponse<Patient>>(`${this.apiUrl}/${id}`, patient);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
