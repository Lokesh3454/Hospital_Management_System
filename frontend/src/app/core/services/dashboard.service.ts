import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminDashboard, DoctorDashboard, PatientDashboard, ReceptionistDashboard } from '../../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  private adminCache: { data: AdminDashboard; timestamp: number } | null = null;
  private doctorCache: { data: DoctorDashboard; timestamp: number } | null = null;
  private patientCache: { data: PatientDashboard; timestamp: number } | null = null;
  private receptionistCache: { data: ReceptionistDashboard; timestamp: number } | null = null;
  private readonly CACHE_TTL_MS = 25000; // 25 seconds

  constructor(private http: HttpClient) {}

  getAdminDashboard(forceRefresh = false): Observable<AdminDashboard> {
    const now = Date.now();
    if (!forceRefresh && this.adminCache && (now - this.adminCache.timestamp < this.CACHE_TTL_MS)) {
      return of(this.adminCache.data);
    }
    return this.http.get<AdminDashboard>(`${this.apiUrl}/admin`).pipe(
      tap(data => {
        this.adminCache = { data, timestamp: Date.now() };
      })
    );
  }

  getDoctorDashboard(forceRefresh = false): Observable<DoctorDashboard> {
    const now = Date.now();
    if (!forceRefresh && this.doctorCache && (now - this.doctorCache.timestamp < this.CACHE_TTL_MS)) {
      return of(this.doctorCache.data);
    }
    return this.http.get<DoctorDashboard>(`${this.apiUrl}/doctor`).pipe(
      tap(data => {
        this.doctorCache = { data, timestamp: Date.now() };
      })
    );
  }

  getPatientDashboard(forceRefresh = false): Observable<PatientDashboard> {
    const now = Date.now();
    if (!forceRefresh && this.patientCache && (now - this.patientCache.timestamp < this.CACHE_TTL_MS)) {
      return of(this.patientCache.data);
    }
    return this.http.get<PatientDashboard>(`${this.apiUrl}/patient`).pipe(
      tap(data => {
        this.patientCache = { data, timestamp: Date.now() };
      })
    );
  }

  getReceptionistDashboard(forceRefresh = false): Observable<ReceptionistDashboard> {
    const now = Date.now();
    if (!forceRefresh && this.receptionistCache && (now - this.receptionistCache.timestamp < this.CACHE_TTL_MS)) {
      return of(this.receptionistCache.data);
    }
    return this.http.get<ReceptionistDashboard>(`${this.apiUrl}/receptionist`).pipe(
      tap(data => {
        this.receptionistCache = { data, timestamp: Date.now() };
      })
    );
  }
}
