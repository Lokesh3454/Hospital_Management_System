import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DoctorService } from '../../../core/services/doctor.service';
import { AuthService } from '../../../core/services/auth.service';
import { Doctor } from '../../../models/doctor.model';

@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './doctor-list.component.html',
  styleUrls: ['./doctor-list.component.css']
})
export class DoctorListComponent implements OnInit {
  doctors: Doctor[] = [];
  specializations: string[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Search & Filter state
  searchTerm = '';
  filterName = '';
  selectedSpecialization = '';
  filterExperience: number | null = null;
  selectedStatus = '';
  viewMode: 'grid' | 'table' = 'grid';

  constructor(
    private doctorService: DoctorService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadSpecializations();
    this.loadDoctors();
  }

  get isAdmin(): boolean {
    return this.authService.hasRole('ROLE_ADMIN');
  }

  loadSpecializations(): void {
    this.doctorService.getSpecializations().subscribe({
      next: (res) => {
        this.specializations = res.data || [];
      },
      error: () => {}
    });
  }

  loadDoctors(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.doctorService.getAll(
      this.searchTerm,
      this.selectedSpecialization,
      this.selectedStatus,
      this.filterName,
      this.filterExperience !== null ? this.filterExperience : undefined
    ).subscribe({
      next: (res) => {
        this.doctors = res.data || [];
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load doctors: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  onFilterChange(): void {
    this.loadDoctors();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.filterName = '';
    this.selectedSpecialization = '';
    this.filterExperience = null;
    this.selectedStatus = '';
    this.loadDoctors();
  }

  deleteDoctor(id: number, name: string): void {
    if (confirm(`Are you sure you want to remove ${name} from the medical directory?`)) {
      this.doctorService.delete(id).subscribe({
        next: () => {
          this.successMessage = `Doctor "${name}" deleted successfully.`;
          this.loadDoctors();
          setTimeout(() => (this.successMessage = null), 3000);
        },
        error: (err) => {
          this.errorMessage = 'Could not delete doctor: ' + (err.error?.message || err.message);
        }
      });
    }
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'AVAILABLE': return 'bg-success';
      case 'ON_LEAVE': return 'bg-warning text-dark';
      case 'BUSY': return 'bg-danger';
      case 'INACTIVE': return 'bg-secondary';
      default: return 'bg-info text-dark';
    }
  }
}
