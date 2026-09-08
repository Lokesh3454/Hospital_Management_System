import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { StaffShift } from '../../models/staff-roster.model';

@Injectable({
  providedIn: 'root'
})
export class StaffRosterService {
  private apiUrl = `${environment.apiUrl}/staff-roster`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<StaffShift[]>> {
    return this.http.get<ApiResponse<StaffShift[]>>(this.apiUrl);
  }

  getByDay(dayOfWeek: string): Observable<ApiResponse<StaffShift[]>> {
    return this.http.get<ApiResponse<StaffShift[]>>(`${this.apiUrl}/day/${dayOfWeek}`);
  }

  getOnCall(): Observable<ApiResponse<StaffShift[]>> {
    return this.http.get<ApiResponse<StaffShift[]>>(`${this.apiUrl}/on-call`);
  }

  saveShift(shift: StaffShift): Observable<ApiResponse<StaffShift>> {
    return this.http.post<ApiResponse<StaffShift>>(this.apiUrl, shift);
  }

  toggleOnCall(id: number, onCall: boolean): Observable<ApiResponse<StaffShift>> {
    const params = new HttpParams().set('onCall', onCall.toString());
    return this.http.put<ApiResponse<StaffShift>>(`${this.apiUrl}/${id}/on-call`, null, { params });
  }
}
