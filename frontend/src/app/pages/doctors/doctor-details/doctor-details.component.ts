import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DoctorService } from '../../../core/services/doctor.service';
import { AvailabilityService } from '../../../core/services/availability.service';
import { AuthService } from '../../../core/services/auth.service';
import { Doctor } from '../../../models/doctor.model';
import { DoctorAvailability } from '../../../models/availability.model';

@Component({
  selector: 'app-doctor-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './doctor-details.component.html',
  styleUrls: ['./doctor-details.component.css']
})
export class DoctorDetailsComponent implements OnInit {
  doctor: Doctor | null = null;
  availabilities: DoctorAvailability[] = [];
  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private doctorService: DoctorService,
    private availabilityService: AvailabilityService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadDoctor(+id);
    }
  }

  get isAdmin(): boolean {
    return this.authService.hasRole('ROLE_ADMIN');
  }

  loadDoctor(id: number): void {
    this.isLoading = true;
    this.doctorService.getById(id).subscribe({
      next: (res) => {
        this.doctor = res.data || null;
        this.loadAvailabilities(id);
      },
      error: (err) => {
        this.errorMessage = 'Could not load doctor: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  loadAvailabilities(doctorId: number): void {
    this.availabilityService.getByDoctor(doctorId).subscribe({
      next: (res) => {
        this.availabilities = res.data || [];
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  deleteDoctor(): void {
    if (!this.doctor || !this.doctor.doctorId) return;
    if (confirm(`Are you sure you want to remove Dr. ${this.doctor.name} from hospital records?`)) {
      this.doctorService.delete(this.doctor.doctorId).subscribe({
        next: () => {
          this.router.navigate(['/doctors']);
        },
        error: (err) => {
          this.errorMessage = 'Delete failed: ' + (err.error?.message || err.message);
        }
      });
    }
  }
}
