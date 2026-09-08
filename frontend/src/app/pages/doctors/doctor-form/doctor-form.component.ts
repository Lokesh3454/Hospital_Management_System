import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DoctorService } from '../../../core/services/doctor.service';

@Component({
  selector: 'app-doctor-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './doctor-form.component.html',
  styleUrls: ['./doctor-form.component.css']
})
export class DoctorFormComponent implements OnInit {
  doctorForm!: FormGroup;
  isEditMode = false;
  doctorId: number | null = null;
  isLoading = false;
  isSaving = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  specializationOptions = [
    'Cardiology',
    'Neurology',
    'Orthopedics',
    'Pediatrics',
    'General Medicine',
    'Dermatology',
    'Oncology',
    'Gastroenterology',
    'Psychiatry',
    'Ophthalmology'
  ];

  constructor(
    private fb: FormBuilder,
    private doctorService: DoctorService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.doctorId = +idParam;
      this.loadDoctorData(this.doctorId);
    }
  }

  initForm(): void {
    this.doctorForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern(/^[+0-9\s-]{7,20}$/)]],
      specialization: ['Cardiology', [Validators.required]],
      qualification: ['MD, FACC'],
      experience: [5, [Validators.required, Validators.min(0)]],
      consultationFee: [500, [Validators.required, Validators.min(0)]],
      status: ['AVAILABLE', [Validators.required]],
      department: ['Cardiology'],
      roomNumber: ['Room 301']
    });
  }

  loadDoctorData(id: number): void {
    this.isLoading = true;
    this.doctorService.getById(id).subscribe({
      next: (res) => {
        const d = res.data;
        if (d) {
          this.doctorForm.patchValue({
            name: d.name,
            email: d.email,
            phone: d.phone,
            specialization: d.specialization,
            qualification: d.qualification,
            experience: d.experience,
            consultationFee: d.consultationFee,
            status: d.status || 'AVAILABLE',
            department: d.department,
            roomNumber: d.roomNumber
          });
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load doctor: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.doctorForm.invalid) {
      this.doctorForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.errorMessage = null;
    const formVal = this.doctorForm.value;

    if (this.isEditMode && this.doctorId) {
      this.doctorService.update(this.doctorId, formVal).subscribe({
        next: () => {
          this.isSaving = false;
          this.successMessage = 'Doctor profile updated successfully!';
          setTimeout(() => this.router.navigate(['/doctors', this.doctorId]), 1200);
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = 'Update failed: ' + (err.error?.message || err.message);
        }
      });
    } else {
      this.doctorService.create(formVal).subscribe({
        next: (res) => {
          this.isSaving = false;
          this.successMessage = 'Doctor created successfully with initial availability slots!';
          const newId = res.data?.doctorId;
          setTimeout(() => {
            if (newId) {
              this.router.navigate(['/doctors', newId]);
            } else {
              this.router.navigate(['/doctors']);
            }
          }, 1200);
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = 'Creation failed: ' + (err.error?.message || err.message);
        }
      });
    }
  }
}
