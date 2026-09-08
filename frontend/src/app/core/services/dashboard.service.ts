import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminDashboard, DoctorDashboard, PatientDashboard, ReceptionistDashboard } from '../../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getAdminDashboard(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(`${this.apiUrl}/admin`);
  }

  getDoctorDashboard(): Observable<DoctorDashboard> {
    return this.http.get<DoctorDashboard>(`${this.apiUrl}/doctor`);
  }

  getPatientDashboard(): Observable<PatientDashboard> {
    return this.http.get<PatientDashboard>(`${this.apiUrl}/patient`);
  }

  getReceptionistDashboard(): Observable<ReceptionistDashboard> {
    return this.http.get<ReceptionistDashboard>(`${this.apiUrl}/receptionist`);
  }
}
