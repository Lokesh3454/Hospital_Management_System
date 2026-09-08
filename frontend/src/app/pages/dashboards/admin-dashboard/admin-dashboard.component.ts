import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { AdminDashboard } from '../../../models/dashboard.model';
import { forceScrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  dashboard: AdminDashboard | null = null;
  isLoading = false;
  errorMessage = '';

  constructor(
    public authService: AuthService,
    private dashboardService: DashboardService
  ) {}

  ngOnInit(): void {
    forceScrollToTop();
    this.loadDashboardData();
  }

  loadDashboardData(forceRefresh = false): void {
    if (!this.dashboard || forceRefresh) {
      this.isLoading = true;
    }
    this.errorMessage = '';

    this.dashboardService.getAdminDashboard(forceRefresh).subscribe({
      next: (data) => {
        this.dashboard = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load live admin metrics: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }
}
