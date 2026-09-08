import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard-redirect',
  standalone: true,
  template: `
    <div class="d-flex justify-content-center align-items-center vh-100 bg-light">
      <div class="text-center">
        <div class="spinner-border text-primary mb-3" style="width: 3rem; height: 3rem;" role="status"></div>
        <p class="text-muted fw-semibold">Loading Dashboard...</p>
      </div>
    </div>
  `
})
export class DashboardRedirectComponent implements OnInit {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const targetRoute = this.authService.getDashboardRouteForUser();
    this.router.navigateByUrl(targetRoute, { replaceUrl: true });
  }
}
