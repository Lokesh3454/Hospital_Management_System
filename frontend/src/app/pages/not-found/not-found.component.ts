import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container py-5 text-center">
      <div class="row justify-content-center">
        <div class="col-md-6">
          <div class="card border-0 shadow-lg rounded-4 p-5">
            <div class="display-1 text-primary mb-3">
              <i class="bi bi-question-circle"></i>
            </div>
            <h2 class="fw-bold text-dark">404 - Page Not Found</h2>
            <p class="text-muted mb-4">
              The requested hospital page or resource does not exist.
            </p>
            <div>
              <a routerLink="/" class="btn btn-primary px-4 py-2 rounded-pill">
                <i class="bi bi-house me-1"></i> Back to Home
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class NotFoundComponent {}
