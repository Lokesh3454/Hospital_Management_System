import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, ActivatedRoute } from '@angular/router';
import { AppointmentService } from '../../../core/services/appointment.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { PatientService } from '../../../core/services/patient.service';
import { AuthService } from '../../../core/services/auth.service';
import { Doctor } from '../../../models/doctor.model';
import { Patient } from '../../../models/patient.model';
import { TimeSlot } from '../../../models/appointment.model';

@Component({
  selector: 'app-book-appointment',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, RouterLinkActive],
  templateUrl: './book-appointment.component.html',
  styleUrls: ['./book-appointment.component.css']
})
export class BookAppointmentComponent implements OnInit {
  bookingForm!: FormGroup;
  doctors: Doctor[] = [];
  patients: Patient[] = [];
  selectedDoctor: Doctor | null = null;
  availableSlots: TimeSlot[] = [];

  isLoadingData = false;
  isLoadingSlots = false;
  isSubmitting = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  minDate = '';

  constructor(
    private fb: FormBuilder,
    private appointmentService: AppointmentService,
    private doctorService: DoctorService,
    private patientService: PatientService,
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Set min date to today
    const today = new Date();
    this.minDate = today.toISOString().split('T')[0];

    // Default to tomorrow or next day
    const defaultDate = new Date();
    defaultDate.setDate(defaultDate.getDate() + 1);
    const formattedDefaultDate = defaultDate.toISOString().split('T')[0];

    this.bookingForm = this.fb.group({
      doctorId: ['', [Validators.required]],
      appointmentDate: [formattedDefaultDate, [Validators.required]],
      appointmentTime: ['', [Validators.required]],
      patientId: [null],
      reason: ['', [Validators.required, Validators.minLength(5)]],
      notes: ['']
    });

    // If patient role, patientId can be null (auto-resolved) or prefilled from auth profile
    if (!this.isPatient) {
      this.bookingForm.get('patientId')?.setValidators([Validators.required]);
    } else {
      const user = this.authService.currentUserValue;
      if (user?.profileId) {
        this.bookingForm.patchValue({ patientId: user.profileId });
      }
    }

    this.loadInitialData();

    // Watch doctorId and appointmentDate changes to load slots
    this.bookingForm.get('doctorId')?.valueChanges.subscribe(doctorId => {
      this.onDoctorSelected(Number(doctorId));
    });

    this.bookingForm.get('appointmentDate')?.valueChanges.subscribe(date => {
      this.loadSlots();
    });
  }

  get isPatient(): boolean {
    return this.authService.hasRole('ROLE_PATIENT');
  }

  get isDoctor(): boolean {
    return this.authService.hasRole('ROLE_DOCTOR');
  }

  get canBook(): boolean {
    return this.authService.hasAnyRole(['ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_PATIENT']);
  }

  loadInitialData(): void {
    this.isLoadingData = true;

    this.doctorService.getAll().subscribe({
      next: (res) => {
        this.doctors = (res.data || []).filter(d => d.status !== 'INACTIVE');

        // Check if doctorId was passed in query params (e.g. from doctor profile "Book Appointment")
        this.route.queryParams.subscribe(params => {
          if (params['doctorId']) {
            const preselectedDocId = Number(params['doctorId']);
            this.bookingForm.patchValue({ doctorId: preselectedDocId });
          } else if (this.doctors.length > 0) {
            this.bookingForm.patchValue({ doctorId: this.doctors[0].doctorId });
          }
        });

        this.isLoadingData = false;
      },
      error: () => {
        this.isLoadingData = false;
      }
    });

    if (!this.isPatient) {
      this.patientService.getAll().subscribe({
        next: (res) => {
          this.patients = res.data || [];
          if (this.patients.length > 0 && !this.bookingForm.get('patientId')?.value) {
            this.bookingForm.patchValue({ patientId: this.patients[0].patientId });
          }
        },
        error: () => {}
      });
    }
  }

  onDoctorSelected(doctorId: number): void {
    this.selectedDoctor = this.doctors.find(d => d.doctorId === doctorId) || null;
    this.bookingForm.patchValue({ appointmentTime: '' });
    this.loadSlots();
  }

  loadSlots(): void {
    const doctorId = this.bookingForm.get('doctorId')?.value;
    const date = this.bookingForm.get('appointmentDate')?.value;

    if (!doctorId || !date) {
      this.availableSlots = [];
      return;
    }

    this.isLoadingSlots = true;
    this.errorMessage = null;
    this.bookingForm.patchValue({ appointmentTime: '' });

    this.appointmentService.getAvailableSlots(Number(doctorId), date).subscribe({
      next: (res) => {
        this.availableSlots = res.data || [];
        this.isLoadingSlots = false;
      },
      error: (err) => {
        this.availableSlots = [];
        this.isLoadingSlots = false;
      }
    });
  }

  selectSlot(slot: TimeSlot): void {
    if (slot.available) {
      this.bookingForm.patchValue({ appointmentTime: slot.time });
    }
  }

  onSubmit(): void {
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      this.errorMessage = 'Please complete all required fields and select an available time slot.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    const formVal = this.bookingForm.value;
    const request = {
      doctorId: Number(formVal.doctorId),
      patientId: formVal.patientId ? Number(formVal.patientId) : undefined,
      appointmentDate: formVal.appointmentDate,
      appointmentTime: formVal.appointmentTime,
      reason: formVal.reason,
      notes: formVal.notes
    };

    this.appointmentService.book(request).subscribe({
      next: (res) => {
        this.successMessage = 'Appointment booked successfully! Redirecting...';
        this.isSubmitting = false;
        setTimeout(() => {
          this.router.navigate(['/appointments']);
        }, 1500);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to book appointment. Please try again.';
        this.isSubmitting = false;
      }
    });
  }

  getSelectedSlotFormatted(): string {
    const time = this.bookingForm.get('appointmentTime')?.value;
    if (!time) return 'Not selected';
    const matching = this.availableSlots.find(s => s.time === time);
    return matching ? matching.formattedTime : time;
  }
}
