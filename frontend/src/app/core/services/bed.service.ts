import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { Bed } from '../../models/bed.model';

@Injectable({
  providedIn: 'root'
})
export class BedService {
  private apiUrl = `${environment.apiUrl}/beds`;

  constructor(private http: HttpClient) {}

  getAllBeds(): Observable<ApiResponse<Bed[]>> {
    return this.http.get<ApiResponse<Bed[]>>(this.apiUrl);
  }

  getBedById(id: number): Observable<ApiResponse<Bed>> {
    return this.http.get<ApiResponse<Bed>>(`${this.apiUrl}/${id}`);
  }

  createBed(bed: Bed): Observable<ApiResponse<Bed>> {
    return this.http.post<ApiResponse<Bed>>(this.apiUrl, bed);
  }

  updateBed(id: number, bed: Bed): Observable<ApiResponse<Bed>> {
    return this.http.put<ApiResponse<Bed>>(`${this.apiUrl}/${id}`, bed);
  }

  assignPatient(id: number, patientId: number, notes?: string): Observable<ApiResponse<Bed>> {
    return this.http.post<ApiResponse<Bed>>(`${this.apiUrl}/${id}/assign`, { patientId, notes });
  }

  dischargePatient(id: number): Observable<ApiResponse<Bed>> {
    return this.http.post<ApiResponse<Bed>>(`${this.apiUrl}/${id}/discharge`, {});
  }

  deleteBed(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
