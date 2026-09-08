import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { PatientService } from '../../../core/services/patient.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { AuthService } from '../../../core/services/auth.service';
import { Patient } from '../../../models/patient.model';
import { Doctor } from '../../../models/doctor.model';

@Component({
  selector: 'app-patient-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './patient-list.component.html',
  styleUrls: ['./patient-list.component.css']
})
export class PatientListComponent implements OnInit {
  patients: Patient[] = [];
  doctors: Doctor[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Search and Filter fields
  searchTerm = '';
  filterName = '';
  filterPhone = '';
  filterEmail = '';
  selectedGender = '';
  selectedBloodGroup = '';
  selectedDoctorId: number | null = null;

  constructor(
    private patientService: PatientService,
    private doctorService: DoctorService,
    public authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDoctors();

    this.route.queryParams.subscribe(params => {
      if (params['search']) this.searchTerm = params['search'];
      if (params['name']) this.filterName = params['name'];
      if (params['phone']) this.filterPhone = params['phone'];
      if (params['email']) this.filterEmail = params['email'];
      if (params['gender']) this.selectedGender = params['gender'];
      if (params['bloodGroup']) this.selectedBloodGroup = params['bloodGroup'];
      if (params['doctorId']) {
        this.selectedDoctorId = +params['doctorId'];
      } else {
        this.selectedDoctorId = null;
      }
      this.loadPatients();
    });
  }

  loadDoctors(): void {
    this.doctorService.getAll().subscribe({
      next: (res) => {
        this.doctors = res.data || [];
      },
      error: () => {
        // Non-critical, filter dropdown will remain empty if fails
      }
    });
  }

  get isDoctor(): boolean {
    return this.authService.hasRole('ROLE_DOCTOR');
  }

  get currentDoctorProfileId(): number | null {
    return this.authService.currentUserValue?.profileId || null;
  }

  get isMyPatientsActive(): boolean {
    if (!this.isDoctor || !this.currentDoctorProfileId) return false;
    return this.selectedDoctorId === this.currentDoctorProfileId;
  }

  setDoctorScope(mode: 'my' | 'all'): void {
    if (mode === 'my' && this.currentDoctorProfileId) {
      this.selectedDoctorId = this.currentDoctorProfileId;
    } else {
      this.selectedDoctorId = null;
    }

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        doctorId: this.selectedDoctorId ? this.selectedDoctorId : null
      },
      queryParamsHandling: 'merge'
    });
    this.loadPatients();
  }

  get canManagePatients(): boolean {
    return this.authService.hasRole('ROLE_ADMIN') || this.authService.hasRole('ROLE_RECEPTIONIST');
  }

  get canDeletePatient(): boolean {
    return this.authService.hasRole('ROLE_ADMIN') || this.authService.hasRole('ROLE_DOCTOR');
  }

  loadPatients(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.patientService.getAll(
      this.searchTerm,
      this.selectedGender,
      this.selectedBloodGroup,
      this.filterName,
      this.filterPhone,
      this.filterEmail,
      this.selectedDoctorId ? this.selectedDoctorId : undefined
    ).subscribe({
      next: (res) => {
        this.patients = res.data || [];
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load patients. ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  onFilterChange(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        search: this.searchTerm ? this.searchTerm : null,
        name: this.filterName ? this.filterName : null,
        phone: this.filterPhone ? this.filterPhone : null,
        email: this.filterEmail ? this.filterEmail : null,
        gender: this.selectedGender ? this.selectedGender : null,
        bloodGroup: this.selectedBloodGroup ? this.selectedBloodGroup : null,
        doctorId: this.selectedDoctorId ? this.selectedDoctorId : null
      },
      queryParamsHandling: 'merge'
    });
    this.loadPatients();
  }

  get isAnyFilterActive(): boolean {
    return !!this.searchTerm || !!this.filterName || !!this.filterPhone || !!this.filterEmail ||
           !!this.selectedGender || !!this.selectedBloodGroup || !!this.selectedDoctorId;
  }

  get activeFilterTitle(): string {
    const parts: string[] = [];
    if (this.isDoctor && this.selectedDoctorId === this.currentDoctorProfileId) {
      parts.push('Patients In My Care');
    } else if (this.selectedDoctorId) {
      const doc = this.doctors.find(d => d.doctorId === this.selectedDoctorId);
      parts.push(doc ? `Doctor: Dr. ${doc.name}` : `Doctor ID: #${this.selectedDoctorId}`);
    }
    if (this.filterName) parts.push(`Name: ${this.filterName}`);
    if (this.searchTerm) parts.push(`Search: "${this.searchTerm}"`);
    if (this.filterPhone) parts.push(`Phone: ${this.filterPhone}`);
    if (this.selectedGender) parts.push(`Gender: ${this.selectedGender}`);
    if (this.selectedBloodGroup) parts.push(`Blood: ${this.selectedBloodGroup}`);
    return parts.join(' | ') || 'All Patients';
  }

  patientToDelete: any | null = null;
  isDeleting = false;

  requestDeletePatient(p: any): void {
    this.patientToDelete = p;
    this.errorMessage = null;
  }

  cancelDeletePatient(): void {
    this.patientToDelete = null;
  }

  confirmDeletePatient(): void {
    if (!this.patientToDelete || !this.patientToDelete.patientId) return;
    const pId: number = this.patientToDelete.patientId;
    const pName = this.patientToDelete.name;
    this.isDeleting = true;
    this.errorMessage = null;

    this.patientService.delete(pId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.patientToDelete = null;
        this.successMessage = `Patient "${pName}" deleted successfully.`;
        this.loadPatients();
        setTimeout(() => (this.successMessage = null), 4000);
      },
      error: (err) => {
        this.isDeleting = false;
        this.errorMessage = 'Could not delete patient: ' + (err.error?.message || err.message);
      }
    });
  }
}
