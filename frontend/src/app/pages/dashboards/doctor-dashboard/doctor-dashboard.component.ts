import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { DoctorDashboard } from '../../../models/dashboard.model';
import { forceScrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './doctor-dashboard.component.html',
  styleUrls: ['./doctor-dashboard.component.css']
})
export class DoctorDashboardComponent implements OnInit {
  dashboard: DoctorDashboard | null = null;
  isLoading = false;
  errorMessage = '';
  activeQueueTab: 'today' | 'upcoming' = 'today';

  constructor(
    public authService: AuthService,
    private dashboardService: DashboardService
  ) {}

  setQueueTab(tab: 'today' | 'upcoming'): void {
    this.activeQueueTab = tab;
  }

  ngOnInit(): void {
    forceScrollToTop();
    this.loadDoctorMetrics();
  }

  loadDoctorMetrics(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.dashboardService.getDoctorDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load doctor clinical dashboard: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }
}
