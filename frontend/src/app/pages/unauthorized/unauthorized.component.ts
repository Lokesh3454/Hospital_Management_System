import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container py-5 text-center">
      <div class="row justify-content-center">
        <div class="col-md-6">
          <div class="card border-0 shadow-lg rounded-4 p-5">
            <div class="display-1 text-danger mb-3">
              <i class="bi bi-shield-x"></i>
            </div>
            <h2 class="fw-bold text-dark">403 - Access Denied</h2>
            <p class="text-muted mb-4">
              You do not have the required permissions or role authorization to access this clinical module or administrative page.
            </p>
            <div>
              <a [routerLink]="authService.getDashboardRouteForUser()" class="btn btn-primary px-4 py-2 rounded-pill">
                <i class="bi bi-arrow-left me-1"></i> Return to My Dashboard
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .display-1 {
      font-size: 5rem;
    }
  `]
})
export class UnauthorizedComponent {
  constructor(public authService: AuthService) {}
}
