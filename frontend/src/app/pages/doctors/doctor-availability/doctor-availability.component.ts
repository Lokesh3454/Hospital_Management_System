import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DoctorService } from '../../../core/services/doctor.service';
import { AvailabilityService } from '../../../core/services/availability.service';
import { AuthService } from '../../../core/services/auth.service';
import { Doctor } from '../../../models/doctor.model';
import { DoctorAvailability } from '../../../models/availability.model';

@Component({
  selector: 'app-doctor-availability',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './doctor-availability.component.html',
  styleUrls: ['./doctor-availability.component.css']
})
export class DoctorAvailabilityComponent implements OnInit {
  doctorId!: number;
  doctor: Doctor | null = null;
  availabilities: DoctorAvailability[] = [];
  slotForm!: FormGroup;

  isLoading = false;
  isSaving = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  editingSlotId: number | null = null;

  daysOfWeek = [
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
    'SUNDAY'
  ];

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private doctorService: DoctorService,
    private availabilityService: AvailabilityService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.doctorId = +idParam;
      this.initForm();
      this.loadDoctorInfo();
      this.loadAvailabilities();
    }
  }

  get canManage(): boolean {
    return this.authService.hasRole('ROLE_ADMIN') || this.authService.hasRole('ROLE_DOCTOR');
  }

  initForm(): void {
    this.slotForm = this.fb.group({
      dayOfWeek: ['MONDAY', [Validators.required]],
      startTime: ['09:00', [Validators.required]],
      endTime: ['17:00', [Validators.required]],
      available: [true]
    });
  }

  loadDoctorInfo(): void {
    this.doctorService.getById(this.doctorId).subscribe({
      next: (res) => (this.doctor = res.data || null),
      error: () => {}
    });
  }

  loadAvailabilities(): void {
    this.isLoading = true;
    this.availabilityService.getByDoctor(this.doctorId).subscribe({
      next: (res) => {
        this.availabilities = res.data || [];
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load availability slots: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.slotForm.invalid) {
      this.slotForm.markAllAsTouched();
      return;
    }

    const formVal = this.slotForm.value;
    if (formVal.startTime >= formVal.endTime) {
      this.errorMessage = 'Start time must be before end time.';
      return;
    }

    this.isSaving = true;
    this.errorMessage = null;

    if (this.editingSlotId) {
      this.availabilityService.update(this.editingSlotId, formVal).subscribe({
        next: () => {
          this.isSaving = false;
          this.successMessage = 'Availability slot updated!';
          this.cancelEdit();
          this.loadAvailabilities();
          setTimeout(() => (this.successMessage = null), 3000);
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = 'Update error: ' + (err.error?.message || err.message);
        }
      });
    } else {
      this.availabilityService.create(this.doctorId, formVal).subscribe({
        next: () => {
          this.isSaving = false;
          this.successMessage = 'Availability slot added successfully!';
          this.slotForm.reset({ dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00', available: true });
          this.loadAvailabilities();
          setTimeout(() => (this.successMessage = null), 3000);
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = 'Add slot failed: ' + (err.error?.message || err.message);
        }
      });
    }
  }

  startEdit(slot: DoctorAvailability): void {
    this.editingSlotId = slot.availabilityId || null;
    this.slotForm.patchValue({
      dayOfWeek: slot.dayOfWeek,
      startTime: slot.startTime.substring(0, 5),
      endTime: slot.endTime.substring(0, 5),
      available: slot.available
    });
  }

  cancelEdit(): void {
    this.editingSlotId = null;
    this.slotForm.reset({ dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00', available: true });
  }

  toggleStatus(slot: DoctorAvailability): void {
    if (!slot.availabilityId) return;
    const updated = { ...slot, available: !slot.available };
    this.availabilityService.update(slot.availabilityId, updated).subscribe({
      next: () => {
        slot.available = !slot.available;
      },
      error: (err) => {
        this.errorMessage = 'Could not toggle availability: ' + (err.error?.message || err.message);
      }
    });
  }

  deleteSlot(id: number): void {
    if (confirm('Are you sure you want to delete this availability slot?')) {
      this.availabilityService.delete(id).subscribe({
        next: () => {
          this.loadAvailabilities();
        },
        error: (err) => {
          this.errorMessage = 'Delete failed: ' + (err.error?.message || err.message);
        }
      });
    }
  }
}
