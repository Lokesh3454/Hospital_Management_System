import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Bill, BillRequest, PaymentUpdate } from '../../models/bill.model';

@Injectable({
  providedIn: 'root'
})
export class BillService {
  private apiUrl = `${environment.apiUrl}/bills`;

  constructor(private http: HttpClient) {}

  getAll(filters?: {
    search?: string;
    patientId?: number;
    date?: string;
    paymentStatus?: string;
  }): Observable<Bill[]> {
    let params = new HttpParams();
    if (filters?.search && filters.search.trim()) {
      params = params.set('search', filters.search.trim());
    }
    if (filters?.patientId) {
      params = params.set('patientId', filters.patientId.toString());
    }
    if (filters?.date) {
      params = params.set('date', filters.date);
    }
    if (filters?.paymentStatus && filters.paymentStatus.trim()) {
      params = params.set('paymentStatus', filters.paymentStatus.trim());
    }

    return this.http.get<Bill[]>(this.apiUrl, { params });
  }

  getById(id: number): Observable<Bill> {
    return this.http.get<Bill>(`${this.apiUrl}/${id}`);
  }

  getPatientBills(patientId: number): Observable<Bill[]> {
    return this.http.get<Bill[]>(`${this.apiUrl}/patient/${patientId}`);
  }

  create(bill: BillRequest): Observable<Bill> {
    return this.http.post<Bill>(this.apiUrl, bill);
  }

  update(id: number, bill: BillRequest): Observable<Bill> {
    return this.http.put<Bill>(`${this.apiUrl}/${id}`, bill);
  }

  markPaid(id: number, payment: PaymentUpdate): Observable<Bill> {
    return this.http.patch<Bill>(`${this.apiUrl}/${id}/pay`, payment);
  }
}
