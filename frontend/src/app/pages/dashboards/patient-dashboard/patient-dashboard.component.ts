import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { BillService } from '../../../core/services/bill.service';
import { PatientDashboard } from '../../../models/dashboard.model';
import { Bill } from '../../../models/bill.model';
import { forceScrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './patient-dashboard.component.html',
  styleUrls: ['./patient-dashboard.component.css']
})
export class PatientDashboardComponent implements OnInit {
  dashboard: PatientDashboard | null = null;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    public authService: AuthService,
    private dashboardService: DashboardService,
    private billService: BillService
  ) {}

  ngOnInit(): void {
    forceScrollToTop();
    this.loadPatientDashboard();
  }

  loadPatientDashboard(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.dashboardService.getPatientDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load patient dashboard: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  quickPayBill(bill: Bill): void {
    this.billService.markPaid(bill.id, {
      paymentMethod: 'UPI',
      paymentStatus: 'PAID',
      notes: 'Paid from patient portal'
    }).subscribe({
      next: (updated) => {
        this.successMessage = `Bill ${updated.billNumber} paid successfully!`;
        this.loadPatientDashboard();
        setTimeout(() => this.successMessage = '', 4000);
      },
      error: (err) => {
        this.errorMessage = 'Payment failed: ' + (err.error?.message || err.message);
      }
    });
  }
}
