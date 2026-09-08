import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { forceScrollToTop } from '../../shared/utils/scroll.util';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  returnUrl: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    forceScrollToTop();
    if (this.authService.isLoggedIn()) {
      this.router.navigate([this.authService.getDashboardRouteForUser()]);
      return;
    }

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '';

    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(4)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        this.isLoading = false;
        forceScrollToTop();
        if (this.returnUrl) {
          this.router.navigateByUrl(this.returnUrl).then(() => forceScrollToTop());
        } else {
          this.router.navigate([this.authService.getDashboardRouteForUser()]).then(() => forceScrollToTop());
        }
      },
      error: (err) => {
        this.isLoading = false;
        if (err.error && err.error.message) {
          this.errorMessage = err.error.message;
        } else if (err.status === 401) {
          this.errorMessage = 'Invalid username or password. Please try again.';
        } else {
          this.errorMessage = 'Login failed. Please check your backend connection.';
        }
      }
    });
  }

  quickLogin(role: string): void {
    forceScrollToTop();
    switch (role) {
      case 'ADMIN':
        this.loginForm.patchValue({ username: 'admin', password: 'admin123' });
        break;
      case 'DOCTOR':
        this.loginForm.patchValue({ username: 'doctor_smith', password: 'doctor123' });
        break;
      case 'PATIENT':
        this.loginForm.patchValue({ username: 'patient_john', password: 'patient123' });
        break;
      case 'RECEPTIONIST':
        this.loginForm.patchValue({ username: 'receptionist_sarah', password: 'rec123' });
        break;
    }
    this.onSubmit();
  }
}
