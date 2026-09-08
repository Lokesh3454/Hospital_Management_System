import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DoctorAvailability } from '../../models/availability.model';
import { ApiResponse } from '../../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AvailabilityService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getByDoctor(doctorId: number): Observable<ApiResponse<DoctorAvailability[]>> {
    return this.http.get<ApiResponse<DoctorAvailability[]>>(`${this.apiUrl}/doctors/${doctorId}/availabilities`);
  }

  create(doctorId: number, slot: DoctorAvailability): Observable<ApiResponse<DoctorAvailability>> {
    return this.http.post<ApiResponse<DoctorAvailability>>(`${this.apiUrl}/doctors/${doctorId}/availabilities`, slot);
  }

  update(availabilityId: number, slot: DoctorAvailability): Observable<ApiResponse<DoctorAvailability>> {
    return this.http.put<ApiResponse<DoctorAvailability>>(`${this.apiUrl}/availabilities/${availabilityId}`, slot);
  }

  delete(availabilityId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/availabilities/${availabilityId}`);
  }
}
