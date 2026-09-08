import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PatientService } from '../../../core/services/patient.service';
import { AuthService } from '../../../core/services/auth.service';
import { VitalsService } from '../../../core/services/vitals.service';
import { LabTestService } from '../../../core/services/lab-test.service';
import { ClinicalSupportService } from '../../../core/services/clinical-support.service';
import { Patient } from '../../../models/patient.model';
import { Vitals } from '../../../models/vitals.model';
import { LabTest } from '../../../models/lab-test.model';
import { ClinicalBrief } from '../../../models/clinical-support.model';

@Component({
  selector: 'app-patient-details',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './patient-details.component.html',
  styleUrls: ['./patient-details.component.css']
})
export class PatientDetailsComponent implements OnInit {
  patient: Patient | null = null;
  vitalsList: Vitals[] = [];
  latestVitals: Vitals | null = null;
  labTests: LabTest[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  vitalsMessage: string | null = null;

  // Digital Health Card & Telehealth
  showHealthCardModal = false;
  telehealthPosition = 2;
  telehealthActive = false;

  // AI Clinical Brief
  showAiBriefModal = false;
  isLoadingAiBrief = false;
  clinicalBrief: ClinicalBrief | null = null;

  isConfirmingDelete = false;
  isDeleting = false;
  showVitalsModal = false;
  isSavingVitals = false;

  newVitals: Vitals = {
    patientId: 0,
    recordedBy: 'Clinical Staff',
    systolicBP: 120,
    diastolicBP: 80,
    heartRate: 72,
    temperature: 98.6,
    spo2: 99,
    respiratoryRate: 16,
    weightKg: 70,
    heightCm: 170,
    notes: ''
  };

  activeTab: 'overview' | 'vitals' | 'labs' = 'overview';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private patientService: PatientService,
    private vitalsService: VitalsService,
    private labTestService: LabTestService,
    private clinicalSupportService: ClinicalSupportService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadPatient(+id);
      this.loadVitals(+id);
      this.loadLabTests(+id);
    }
  }

  get canManagePatients(): boolean {
    return this.authService.hasRole('ROLE_ADMIN') || this.authService.hasRole('ROLE_RECEPTIONIST');
  }

  get canDeletePatient(): boolean {
    return this.authService.hasRole('ROLE_ADMIN') || this.authService.hasRole('ROLE_DOCTOR');
  }

  get canRecordClinicalData(): boolean {
    return this.authService.hasAnyRole(['ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST']);
  }

  loadPatient(id: number): void {
    this.isLoading = true;
    this.patientService.getById(id).subscribe({
      next: (res) => {
        this.patient = res.data || null;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Could not load patient: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  loadVitals(patientId: number): void {
    this.vitalsService.getPatientVitals(patientId).subscribe({
      next: (res) => {
        this.vitalsList = res.data || [];
        this.latestVitals = this.vitalsList.length > 0 ? this.vitalsList[0] : null;
      },
      error: () => {}
    });
  }

  loadLabTests(patientId: number): void {
    this.labTestService.getLabTestsByPatient(patientId).subscribe({
      next: (res) => {
        this.labTests = res.data || [];
      },
      error: () => {}
    });
  }

  openVitalsModal(): void {
    if (this.patient) {
      this.newVitals.patientId = this.patient.id || this.patient.patientId || 0;
      this.showVitalsModal = true;
      this.vitalsMessage = null;
    }
  }

  closeVitalsModal(): void {
    this.showVitalsModal = false;
  }

  saveVitals(): void {
    if (!this.newVitals.patientId) return;
    this.isSavingVitals = true;
    this.vitalsService.recordVitals(this.newVitals).subscribe({
      next: () => {
        this.isSavingVitals = false;
        this.showVitalsModal = false;
        this.loadVitals(this.newVitals.patientId);
      },
      error: (err) => {
        this.vitalsMessage = 'Failed to record vitals: ' + (err.error?.message || err.message);
        this.isSavingVitals = false;
      }
    });
  }

  requestDeletePatient(): void {
    this.isConfirmingDelete = true;
  }

  cancelDelete(): void {
    this.isConfirmingDelete = false;
  }

  confirmDelete(): void {
    if (!this.patient) return;
    const patientId = this.patient.patientId || this.patient.id;
    if (!patientId) return;

    this.isDeleting = true;
    this.patientService.delete(patientId).subscribe({
      next: () => {
        this.isDeleting = false;
        this.router.navigate(['/patients']);
      },
      error: (err) => {
        this.errorMessage = 'Failed to delete patient: ' + (err.error?.message || err.message);
        this.isDeleting = false;
        this.isConfirmingDelete = false;
      }
    });
  }

  getUhid(): string {
    const idVal = this.patient?.patientId || this.patient?.id || 1001;
    return `AGY-${idVal.toString().padStart(6, '0')}`;
  }

  getPatientAge(): number {
    if (!this.patient?.dateOfBirth) return 35;
    const dob = new Date(this.patient.dateOfBirth);
    const diffMs = Date.now() - dob.getTime();
    const ageDt = new Date(diffMs);
    return Math.abs(ageDt.getUTCFullYear() - 1970) || 35;
  }

  openHealthCardModal(): void {
    this.showHealthCardModal = true;
  }

  closeHealthCardModal(): void {
    this.showHealthCardModal = false;
  }

  printHealthCard(): void {
    window.print();
  }

  joinTelehealthQueue(): void {
    this.telehealthActive = true;
    this.telehealthPosition = Math.floor(Math.random() * 3) + 1;
  }

  leaveTelehealthQueue(): void {
    this.telehealthActive = false;
  }

  openAiBriefModal(): void {
    if (!this.patient) return;
    const pId = this.patient.patientId || this.patient.id;
    if (!pId) return;

    this.showAiBriefModal = true;
    this.isLoadingAiBrief = true;
    this.clinicalSupportService.getPatientBrief(pId).subscribe({
      next: (res) => {
        this.isLoadingAiBrief = false;
        if (res.success && res.data) {
          this.clinicalBrief = res.data;
        }
      },
      error: () => {
        this.isLoadingAiBrief = false;
      }
    });
  }

  closeAiBriefModal(): void {
    this.showAiBriefModal = false;
  }

  getBmiCategory(bmi?: number): { text: string; colorClass: string } {
    if (!bmi) return { text: 'N/A', colorClass: 'text-muted' };
    if (bmi < 18.5) return { text: 'Underweight', colorClass: 'text-info' };
    if (bmi < 25.0) return { text: 'Normal Weight', colorClass: 'text-success' };
    if (bmi < 30.0) return { text: 'Overweight', colorClass: 'text-warning' };
    return { text: 'Obese', colorClass: 'text-danger' };
  }
}
