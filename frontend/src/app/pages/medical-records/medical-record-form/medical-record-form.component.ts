import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MedicalRecordService } from '../../../core/services/medical-record.service';
import { PatientService } from '../../../core/services/patient.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { AuthService } from '../../../core/services/auth.service';
import { Patient } from '../../../models/patient.model';
import { Doctor } from '../../../models/doctor.model';

@Component({
  selector: 'app-medical-record-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './medical-record-form.component.html',
  styleUrls: ['./medical-record-form.component.css']
})
export class MedicalRecordFormComponent implements OnInit {
  recordForm!: FormGroup;
  isEditMode = false;
  recordId: number | null = null;
  patients: Patient[] = [];
  doctors: Doctor[] = [];

  isLoading = false;
  isSubmitting = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private medicalRecordService: MedicalRecordService,
    private patientService: PatientService,
    private doctorService: DoctorService,
    public authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const today = new Date().toISOString().split('T')[0];

    this.recordForm = this.fb.group({
      patientId: ['', [Validators.required]],
      doctorId: ['', [Validators.required]],
      appointmentId: [null],
      diagnosis: ['', [Validators.required, Validators.minLength(3)]],
      symptoms: [''],
      treatment: [''],
      testResults: [''],
      notes: [''],
      recordDate: [today, [Validators.required]]
    });

    this.loadDropdowns();

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.recordId = Number(idParam);
      this.loadRecord(this.recordId);
    } else {
      // Check query params for appointmentId, patientId, doctorId
      this.route.queryParams.subscribe(params => {
        if (params['patientId']) {
          this.recordForm.patchValue({ patientId: Number(params['patientId']) });
        }
        if (params['doctorId']) {
          this.recordForm.patchValue({ doctorId: Number(params['doctorId']) });
        }
        if (params['appointmentId']) {
          this.recordForm.patchValue({ appointmentId: Number(params['appointmentId']) });
        }
      });
    }
  }

  loadDropdowns(): void {
    this.patientService.getAll().subscribe({
      next: (res) => {
        this.patients = res.data || [];
      }
    });

    this.doctorService.getAll().subscribe({
      next: (res) => {
        this.doctors = (res.data || []).filter(d => d.status !== 'INACTIVE');
        // If current user is doctor, auto-set doctorId
        const user = this.authService.currentUserValue;
        if (user?.roles?.includes('ROLE_DOCTOR') && user.profileId) {
          this.recordForm.patchValue({ doctorId: user.profileId });
        }
      }
    });
  }

  loadRecord(id: number): void {
    this.isLoading = true;
    this.medicalRecordService.getById(id).subscribe({
      next: (res) => {
        const rec = res.data;
        if (rec) {
          this.recordForm.patchValue({
            patientId: rec.patientId,
            doctorId: rec.doctorId,
            appointmentId: rec.appointmentId,
            diagnosis: rec.diagnosis,
            symptoms: rec.symptoms,
            treatment: rec.treatment,
            testResults: rec.testResults,
            notes: rec.notes,
            recordDate: rec.recordDate
          });
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load medical record.';
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.recordForm.invalid) {
      this.recordForm.markAllAsTouched();
      this.errorMessage = 'Please fill all required fields correctly.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    const formVal = this.recordForm.value;
    const payload = {
      patientId: Number(formVal.patientId),
      doctorId: Number(formVal.doctorId),
      appointmentId: formVal.appointmentId ? Number(formVal.appointmentId) : undefined,
      diagnosis: formVal.diagnosis,
      symptoms: formVal.symptoms,
      treatment: formVal.treatment,
      testResults: formVal.testResults,
      notes: formVal.notes,
      recordDate: formVal.recordDate
    };

    if (this.isEditMode && this.recordId) {
      this.medicalRecordService.update(this.recordId, payload).subscribe({
        next: () => {
          this.successMessage = 'Medical record updated successfully!';
          this.isSubmitting = false;
          setTimeout(() => this.router.navigate(['/medical-records', this.recordId]), 1200);
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to update medical record.';
          this.isSubmitting = false;
        }
      });
    } else {
      this.medicalRecordService.create(payload).subscribe({
        next: (res) => {
          this.successMessage = 'Medical record created successfully!';
          this.isSubmitting = false;
          const newId = res.data ? res.data.id : null;
          setTimeout(() => {
            if (newId) {
              this.router.navigate(['/medical-records', newId]);
            } else {
              this.router.navigate(['/medical-records']);
            }
          }, 1200);
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to create medical record.';
          this.isSubmitting = false;
        }
      });
    }
  }
}
