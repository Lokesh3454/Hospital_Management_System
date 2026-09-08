import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/auth.model';
import { Medicine } from '../../models/medicine.model';

@Injectable({
  providedIn: 'root'
})
export class MedicineService {
  private apiUrl = `${environment.apiUrl}/medicines`;

  constructor(private http: HttpClient) {}

  getAllMedicines(search?: string): Observable<ApiResponse<Medicine[]>> {
    let params = new HttpParams();
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }
    return this.http.get<ApiResponse<Medicine[]>>(this.apiUrl, { params });
  }

  getLowStockMedicines(): Observable<ApiResponse<Medicine[]>> {
    return this.http.get<ApiResponse<Medicine[]>>(`${this.apiUrl}/low-stock`);
  }

  getMedicineById(id: number): Observable<ApiResponse<Medicine>> {
    return this.http.get<ApiResponse<Medicine>>(`${this.apiUrl}/${id}`);
  }

  createMedicine(med: Medicine): Observable<ApiResponse<Medicine>> {
    return this.http.post<ApiResponse<Medicine>>(this.apiUrl, med);
  }

  updateMedicine(id: number, med: Medicine): Observable<ApiResponse<Medicine>> {
    return this.http.put<ApiResponse<Medicine>>(`${this.apiUrl}/${id}`, med);
  }

  adjustStock(id: number, delta: number): Observable<ApiResponse<Medicine>> {
    return this.http.post<ApiResponse<Medicine>>(`${this.apiUrl}/${id}/adjust-stock`, { delta });
  }

  deleteMedicine(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
