import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { HospitalAnalytics } from '../../models/analytics.model';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private apiUrl = `${environment.apiUrl}/analytics`;

  constructor(private http: HttpClient) {}

  getExecutiveAnalytics(): Observable<ApiResponse<HospitalAnalytics>> {
    return this.http.get<ApiResponse<HospitalAnalytics>>(this.apiUrl);
  }
}
