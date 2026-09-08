import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { NotificationLog } from '../../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<NotificationLog[]>> {
    return this.http.get<ApiResponse<NotificationLog[]>>(this.apiUrl);
  }

  getRecent(): Observable<ApiResponse<NotificationLog[]>> {
    return this.http.get<ApiResponse<NotificationLog[]>>(`${this.apiUrl}/recent`);
  }

  dispatchNotification(notification: NotificationLog): Observable<ApiResponse<NotificationLog>> {
    return this.http.post<ApiResponse<NotificationLog>>(`${this.apiUrl}/dispatch`, notification);
  }
}
