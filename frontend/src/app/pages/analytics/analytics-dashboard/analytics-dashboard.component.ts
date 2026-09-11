import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AnalyticsService } from '../../../core/services/analytics.service';
import { AuthService } from '../../../core/services/auth.service';
import { HospitalAnalytics } from '../../../models/analytics.model';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './analytics-dashboard.component.html',
  styleUrls: ['./analytics-dashboard.component.css']
})
export class AnalyticsDashboardComponent implements OnInit {
  analytics: HospitalAnalytics | null = null;
  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private analyticsService: AnalyticsService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAnalytics();
  }

  loadAnalytics(): void {
    this.isLoading = true;
    this.analyticsService.getExecutiveAnalytics().subscribe({
      next: (res) => {
        this.analytics = res.data || null;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load executive analytics: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  departmentEntries(): { name: string; count: number }[] {
    if (!this.analytics || !this.analytics.appointmentsByDepartment) return [];
    return Object.entries(this.analytics.appointmentsByDepartment).map(([name, count]) => ({ name, count }));
  }

  getDepartmentPercent(count: number): number {
    if (!this.analytics || !this.analytics.totalAppointments || this.analytics.totalAppointments === 0) return 0;
    return Math.round((count / this.analytics.totalAppointments) * 100);
  }

  getMaxRevenue(): number {
    if (!this.analytics || !this.analytics.monthlyTrends || this.analytics.monthlyTrends.length === 0) return 1;
    const max = Math.max(...this.analytics.monthlyTrends.map(t => t.revenue || 0));
    return max > 0 ? max : 1;
  }
}
