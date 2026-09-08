import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { BloodInventory } from '../../models/blood-bank.model';

@Injectable({
  providedIn: 'root'
})
export class BloodBankService {
  private apiUrl = `${environment.apiUrl}/blood-bank`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<BloodInventory[]>> {
    return this.http.get<ApiResponse<BloodInventory[]>>(this.apiUrl);
  }

  getByGroup(bloodGroup: string): Observable<ApiResponse<BloodInventory[]>> {
    return this.http.get<ApiResponse<BloodInventory[]>>(`${this.apiUrl}/group/${encodeURIComponent(bloodGroup)}`);
  }

  getAlerts(): Observable<ApiResponse<BloodInventory[]>> {
    return this.http.get<ApiResponse<BloodInventory[]>>(`${this.apiUrl}/alerts`);
  }

  saveInventory(inventory: BloodInventory): Observable<ApiResponse<BloodInventory>> {
    return this.http.post<ApiResponse<BloodInventory>>(this.apiUrl, inventory);
  }

  reserveUnits(id: number, units: number): Observable<ApiResponse<BloodInventory>> {
    const params = new HttpParams().set('units', units.toString());
    return this.http.put<ApiResponse<BloodInventory>>(`${this.apiUrl}/${id}/reserve`, null, { params });
  }

  restockUnits(id: number, units: number): Observable<ApiResponse<BloodInventory>> {
    const params = new HttpParams().set('units', units.toString());
    return this.http.put<ApiResponse<BloodInventory>>(`${this.apiUrl}/${id}/restock`, null, { params });
  }
}
