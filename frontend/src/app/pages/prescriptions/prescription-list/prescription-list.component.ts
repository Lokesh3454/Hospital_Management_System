import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { PrescriptionService } from '../../../core/services/prescription.service';
import { AuthService } from '../../../core/services/auth.service';
import { Prescription } from '../../../models/prescription.model';
import { forceScrollToTop } from '../../../shared/utils/scroll.util';

@Component({
  selector: 'app-prescription-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './prescription-list.component.html',
  styleUrls: ['./prescription-list.component.css']
})
export class PrescriptionListComponent implements OnInit {
  prescriptions: Prescription[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  searchTerm = '';
  selectedDate = '';
  selectedDoctorId: number | null = null;
  selectedPatientId: number | null = null;
  activeTimeline: 'all' | 'my' | 'today' = 'all';

  constructor(
    private prescriptionService: PrescriptionService,
    public authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    forceScrollToTop();
    this.route.queryParams.subscribe(params => {
      this.selectedDate = params['date'] || '';
      this.searchTerm = params['search'] || '';
      this.selectedDoctorId = params['doctorId'] ? +params['doctorId'] : null;
      this.selectedPatientId = params['patientId'] ? +params['patientId'] : null;

      const todayStr = new Date().toISOString().split('T')[0];
      if (params['filter'] === 'today' || this.selectedDate === todayStr) {
        this.activeTimeline = 'today';
        if (!this.selectedDate) this.selectedDate = todayStr;
      } else if (
        (this.isDoctor && this.selectedDoctorId && this.selectedDoctorId === this.authService.currentUserValue?.profileId) ||
        (this.isPatient && (this.selectedPatientId || params['filter'] === 'my'))
      ) {
        this.activeTimeline = 'my';
      } else if (params['filter'] === 'my') {
        this.activeTimeline = 'my';
      } else {
        this.activeTimeline = 'all';
      }

      this.loadPrescriptions();
    });
  }

  get isDoctor(): boolean {
    return this.authService.hasRole('ROLE_DOCTOR');
  }

  get isAdmin(): boolean {
    return this.authService.hasRole('ROLE_ADMIN');
  }

  get isPatient(): boolean {
    return this.authService.hasRole('ROLE_PATIENT');
  }

  get isReceptionist(): boolean {
    return this.authService.hasRole('ROLE_RECEPTIONIST');
  }

  get canCreate(): boolean {
    return this.isDoctor || this.isAdmin;
  }

  loadPrescriptions(): void {
    this.isLoading = true;
    this.errorMessage = null;

    const filters: any = {};
    if (this.searchTerm) filters.search = this.searchTerm;
    if (this.selectedDate) filters.date = this.selectedDate;
    if (this.selectedDoctorId) filters.doctorId = this.selectedDoctorId;
    if (this.selectedPatientId) filters.patientId = this.selectedPatientId;

    if (this.isPatient && this.authService.currentUserValue?.profileId) {
      filters.patientId = this.authService.currentUserValue.profileId;
    }

    this.prescriptionService.getAll(filters).subscribe({
      next: (res) => {
        this.prescriptions = res.data || [];
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load prescriptions.';
        this.isLoading = false;
      }
    });
  }

  setTimelineFilter(timeline: 'all' | 'my' | 'today'): void {
    this.activeTimeline = timeline;
    const queryParams: any = {};

    if (this.searchTerm) {
      queryParams.search = this.searchTerm;
    }

    if (timeline === 'today') {
      const todayStr = new Date().toISOString().split('T')[0];
      this.selectedDate = todayStr;
      queryParams.filter = 'today';
      queryParams.date = todayStr;
    } else if (timeline === 'my') {
      this.selectedDate = '';
      if (this.isDoctor && this.authService.currentUserValue?.profileId) {
        this.selectedDoctorId = this.authService.currentUserValue.profileId;
        queryParams.doctorId = this.selectedDoctorId;
      } else if (this.isPatient && this.authService.currentUserValue?.profileId) {
        this.selectedPatientId = this.authService.currentUserValue.profileId;
        queryParams.patientId = this.selectedPatientId;
      }
    } else {
      this.selectedDate = '';
      this.selectedDoctorId = null;
      this.selectedPatientId = null;
    }

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
      queryParamsHandling: ''
    });
  }

  applySearch(): void {
    const queryParams: any = {};
    if (this.searchTerm) queryParams.search = this.searchTerm;
    if (this.selectedDate) queryParams.date = this.selectedDate;
    if (this.selectedDoctorId) queryParams.doctorId = this.selectedDoctorId;
    if (this.selectedPatientId) queryParams.patientId = this.selectedPatientId;

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams
    });
  }

  get isAnyFilterActive(): boolean {
    return !!this.searchTerm || !!this.selectedDate || !!this.selectedDoctorId || !!this.selectedPatientId || this.activeTimeline !== 'all';
  }

  get activeFilterTitle(): string {
    const parts: string[] = [];
    if (this.selectedDoctorId) {
      if (this.isDoctor && this.selectedDoctorId === this.authService.currentUserValue?.profileId) {
        parts.push('My Issued Prescriptions');
      } else {
        parts.push(`Doctor ID #${this.selectedDoctorId}`);
      }
    }
    if (this.selectedPatientId) {
      parts.push(this.isPatient ? 'My Prescriptions' : `Patient ID #${this.selectedPatientId}`);
    }
    if (this.selectedDate) {
      const todayStr = new Date().toISOString().split('T')[0];
      parts.push(this.selectedDate === todayStr ? "Today's Consultations" : `Date: ${this.selectedDate}`);
    }
    if (this.searchTerm) {
      parts.push(`Search: "${this.searchTerm}"`);
    }
    return parts.join(' | ') || 'All Prescriptions';
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedDate = '';
    this.selectedDoctorId = null;
    this.selectedPatientId = null;
    this.activeTimeline = 'all';

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {}
    });
    this.loadPrescriptions();
  }
}
