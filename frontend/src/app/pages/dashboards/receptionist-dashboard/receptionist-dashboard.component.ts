import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { ReceptionistDashboard } from '../../../models/dashboard.model';
import { forceScrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-receptionist-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './receptionist-dashboard.component.html',
  styleUrls: ['./receptionist-dashboard.component.css']
})
export class ReceptionistDashboardComponent implements OnInit {
  dashboard: ReceptionistDashboard | null = null;
  isLoading = false;
  errorMessage = '';

  constructor(
    public authService: AuthService,
    private dashboardService: DashboardService
  ) {}

  ngOnInit(): void {
    forceScrollToTop();
    this.loadReceptionistData();
  }

  loadReceptionistData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.dashboardService.getReceptionistDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load front desk metrics: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }
}
