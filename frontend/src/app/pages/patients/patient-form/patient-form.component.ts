import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PatientService } from '../../../core/services/patient.service';

@Component({
  selector: 'app-patient-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './patient-form.component.html',
  styleUrls: ['./patient-form.component.css']
})
export class PatientFormComponent implements OnInit {
  patientForm!: FormGroup;
  isEditMode = false;
  patientId: number | null = null;
  isLoading = false;
  isSaving = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private patientService: PatientService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.patientId = +idParam;
      this.loadPatientData(this.patientId);
    }
  }

  initForm(): void {
    this.patientForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      dateOfBirth: [''],
      gender: ['Male', [Validators.required]],
      bloodGroup: ['O+'],
      phone: ['', [Validators.required, Validators.pattern(/^[+0-9\s-]{7,20}$/)]],
      email: ['', [Validators.required, Validators.email]],
      address: [''],
      emergencyContact: ['']
    });
  }

  loadPatientData(id: number): void {
    this.isLoading = true;
    this.patientService.getById(id).subscribe({
      next: (res) => {
        const p = res.data;
        if (p) {
          this.patientForm.patchValue({
            name: p.name,
            dateOfBirth: p.dateOfBirth,
            gender: p.gender || 'Male',
            bloodGroup: p.bloodGroup || 'O+',
            phone: p.phone,
            email: p.email,
            address: p.address,
            emergencyContact: p.emergencyContact
          });
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load patient: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.patientForm.invalid) {
      this.patientForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.errorMessage = null;

    const formVal = this.patientForm.value;

    if (this.isEditMode && this.patientId) {
      this.patientService.update(this.patientId, formVal).subscribe({
        next: () => {
          this.isSaving = false;
          this.successMessage = 'Patient updated successfully! Redirecting...';
          setTimeout(() => this.router.navigate(['/patients', this.patientId]), 1200);
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = 'Update failed: ' + (err.error?.message || err.message);
        }
      });
    } else {
      this.patientService.create(formVal).subscribe({
        next: (res) => {
          this.isSaving = false;
          this.successMessage = 'Patient created successfully! Redirecting...';
          const newId = res.data?.patientId;
          setTimeout(() => {
            if (newId) {
              this.router.navigate(['/patients', newId]);
            } else {
              this.router.navigate(['/patients']);
            }
          }, 1200);
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = 'Registration failed: ' + (err.error?.message || err.message);
        }
      });
    }
  }
}
