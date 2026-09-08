import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Doctor } from '../../models/doctor.model';
import { ApiResponse } from '../../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class DoctorService {
  private apiUrl = `${environment.apiUrl}/doctors`;

  constructor(private http: HttpClient) {}

  getAll(search?: string, specialization?: string, status?: string, name?: string, experience?: number): Observable<ApiResponse<Doctor[]>> {
    let params = new HttpParams();
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }
    if (name && name.trim()) {
      params = params.set('name', name.trim());
    }
    if (specialization && specialization.trim()) {
      params = params.set('specialization', specialization.trim());
    }
    if (experience !== undefined && experience !== null) {
      params = params.set('experience', experience.toString());
    }
    if (status && status.trim()) {
      params = params.set('status', status.trim());
    }

    return this.http.get<ApiResponse<Doctor[]>>(this.apiUrl, { params });
  }

  getSpecializations(): Observable<ApiResponse<string[]>> {
    return this.http.get<ApiResponse<string[]>>(`${this.apiUrl}/specializations`);
  }

  getById(id: number): Observable<ApiResponse<Doctor>> {
    return this.http.get<ApiResponse<Doctor>>(`${this.apiUrl}/${id}`);
  }

  create(doctor: Doctor): Observable<ApiResponse<Doctor>> {
    return this.http.post<ApiResponse<Doctor>>(this.apiUrl, doctor);
  }

  update(id: number, doctor: Doctor): Observable<ApiResponse<Doctor>> {
    return this.http.put<ApiResponse<Doctor>>(`${this.apiUrl}/${id}`, doctor);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
