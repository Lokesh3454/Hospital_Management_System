import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { MedicalRecordService } from '../../../core/services/medical-record.service';
import { AuthService } from '../../../core/services/auth.service';
import { MedicalRecord } from '../../../models/medical-record.model';

import { DoctorService } from '../../../core/services/doctor.service';
import { PatientService } from '../../../core/services/patient.service';
import { Doctor } from '../../../models/doctor.model';

@Component({
  selector: 'app-medical-record-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './medical-record-list.component.html',
  styleUrls: ['./medical-record-list.component.css']
})
export class MedicalRecordListComponent implements OnInit {
  records: MedicalRecord[] = [];
  doctors: Doctor[] = [];
  patients: any[] = [];
  isLoading = false;
  isDeleting = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  searchTerm = '';
  selectedDate = '';
  selectedDoctorId: number | '' = '';
  selectedPatientId: number | '' = '';

  // On-page confirmation states (No browser popups)
  recordToDelete: MedicalRecord | null = null;
  isConfirmingClearAll = false;
  isConfirmingReseed = false;

  constructor(
    private medicalRecordService: MedicalRecordService,
    private doctorService: DoctorService,
    private patientService: PatientService,
    public authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDropdownData();
    this.route.queryParams.subscribe(params => {
      if (params['doctorId']) {
        this.selectedDoctorId = Number(params['doctorId']);
      }
      if (params['patientId']) {
        this.selectedPatientId = Number(params['patientId']);
      }
      if (params['date']) {
        this.selectedDate = params['date'];
      }
      if (params['search']) {
        this.searchTerm = params['search'];
      }
      this.loadRecords();
    });
  }

  loadDropdownData(): void {
    if (!this.isDoctor) {
      this.doctorService.getAll().subscribe({
        next: (res) => this.doctors = res.data || [],
        error: () => {}
      });
    }
    if (!this.isPatient) {
      this.patientService.getAll().subscribe({
        next: (res) => this.patients = res.data || [],
        error: () => {}
      });
    }
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

  get canCreate(): boolean {
    return this.isDoctor || this.isAdmin;
  }

  loadRecords(): void {
    this.isLoading = true;
    this.errorMessage = null;

    const filters: any = {};
    if (this.searchTerm) filters.search = this.searchTerm;
    if (this.selectedDate) filters.date = this.selectedDate;
    if (this.selectedDoctorId) filters.doctorId = Number(this.selectedDoctorId);
    if (this.selectedPatientId) filters.patientId = Number(this.selectedPatientId);

    // If patient, backend will scope to own records, or pass profileId
    if (this.isPatient && this.authService.currentUserValue?.profileId) {
      filters.patientId = this.authService.currentUserValue.profileId;
    }

    this.medicalRecordService.getAll(filters).subscribe({
      next: (res) => {
        this.records = res.data || [];
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load medical records.';
        this.isLoading = false;
      }
    });
  }

  onFilterChange(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        doctorId: this.selectedDoctorId ? this.selectedDoctorId : null,
        patientId: this.selectedPatientId ? this.selectedPatientId : null,
        date: this.selectedDate ? this.selectedDate : null,
        search: this.searchTerm ? this.searchTerm : null
      },
      queryParamsHandling: 'merge'
    });
    this.loadRecords();
  }

  get isAnyFilterActive(): boolean {
    return !!this.searchTerm || !!this.selectedDate || !!this.selectedDoctorId || !!this.selectedPatientId;
  }

  get activeFilterTitle(): string {
    const parts: string[] = [];
    if (this.selectedDoctorId) {
      const doc = this.doctors.find(d => d.doctorId === Number(this.selectedDoctorId));
      if (doc) parts.push(`Dr. ${doc.name}`);
      else parts.push(`Doctor #${this.selectedDoctorId}`);
    }
    if (this.selectedPatientId) {
      const pat = this.patients.find(p => p.id === Number(this.selectedPatientId));
      if (pat) parts.push(`Patient: ${pat.name}`);
      else parts.push(`Patient #${this.selectedPatientId}`);
    }
    if (this.selectedDate) parts.push(`Date: ${this.selectedDate}`);
    if (this.searchTerm) parts.push(`Search: "${this.searchTerm}"`);
    return parts.join(' | ') || 'All Records';
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedDate = '';
    this.selectedDoctorId = '';
    this.selectedPatientId = '';
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {}
    });
    this.loadRecords();
  }

  // ----------------------------------------------------
  // On-Page Record Delete Confirmations (Zero Popups)
  // ----------------------------------------------------
  requestDelete(rec: MedicalRecord): void {
    this.recordToDelete = rec;
    this.isConfirmingClearAll = false;
    this.isConfirmingReseed = false;
    this.errorMessage = null;
  }

  cancelDelete(): void {
    this.recordToDelete = null;
  }

  confirmDelete(): void {
    if (!this.recordToDelete || !this.recordToDelete.id) return;
    const recId: number = this.recordToDelete.id;
    const recDiagnosis = this.recordToDelete.diagnosis;
    this.isDeleting = true;
    this.errorMessage = null;

    this.medicalRecordService.delete(recId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.recordToDelete = null;
        this.successMessage = `Medical record #${recId} (${recDiagnosis}) was deleted successfully.`;
        setTimeout(() => this.successMessage = null, 4000);
        this.loadRecords();
      },
      error: (err) => {
        this.isDeleting = false;
        this.errorMessage = err.error?.message || 'Failed to delete medical record.';
      }
    });
  }

  // ----------------------------------------------------
  // On-Page Reseed Sample Data Confirmations (Zero Popups)
  // ----------------------------------------------------
  requestReseedSampleData(): void {
    this.isConfirmingReseed = true;
    this.isConfirmingClearAll = false;
    this.recordToDelete = null;
    this.errorMessage = null;
  }

  cancelReseed(): void {
    this.isConfirmingReseed = false;
  }

  confirmReseedSampleData(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.isConfirmingReseed = false;

    this.medicalRecordService.seedSampleData().subscribe({
      next: (res) => {
        this.records = res.data || [];
        this.isLoading = false;
        this.successMessage = 'Diverse sample medical records successfully loaded!';
        this.loadDropdownData();
        setTimeout(() => this.successMessage = null, 5000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to seed sample medical records.';
        this.isLoading = false;
      }
    });
  }

  // ----------------------------------------------------
  // On-Page Clear All History Confirmations (Zero Popups)
  // ----------------------------------------------------
  requestClearAllHistory(): void {
    this.isConfirmingClearAll = true;
    this.isConfirmingReseed = false;
    this.recordToDelete = null;
    this.errorMessage = null;
  }

  cancelClearAllHistory(): void {
    this.isConfirmingClearAll = false;
  }

  confirmClearAllHistory(): void {
    this.isDeleting = true;
    this.errorMessage = null;
    this.isConfirmingClearAll = false;

    this.medicalRecordService.clearAllHistory().subscribe({
      next: () => {
        this.records = [];
        this.isDeleting = false;
        this.successMessage = 'All medical records history cleared successfully.';
        setTimeout(() => this.successMessage = null, 4000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to clear medical records.';
        this.isDeleting = false;
      }
    });
  }
}
