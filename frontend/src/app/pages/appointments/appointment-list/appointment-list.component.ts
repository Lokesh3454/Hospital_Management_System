import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive, ActivatedRoute, Router } from '@angular/router';
import { AppointmentService } from '../../../core/services/appointment.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { AuthService } from '../../../core/services/auth.service';
import { Appointment, AppointmentStatus, TimeSlot } from '../../../models/appointment.model';
import { Doctor } from '../../../models/doctor.model';
import { PatientService } from '../../../core/services/patient.service';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './appointment-list.component.html',
  styleUrls: ['./appointment-list.component.css']
})
export class AppointmentListComponent implements OnInit {
  appointments: Appointment[] = [];
  doctors: Doctor[] = [];
  patients: any[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Search & Filter state
  searchTerm = '';
  selectedStatus: AppointmentStatus | '' = '';
  selectedDate = '';
  selectedDoctorId: number | '' = '';
  selectedPatientId: number | '' = '';
  timeFilter: 'all' | 'today' | 'upcoming' | 'history' = 'all';

  // Cancel modal state
  cancelTarget: Appointment | null = null;
  cancelReason = '';
  isCancelling = false;

  // Reschedule modal state
  rescheduleTarget: Appointment | null = null;
  rescheduleDate = '';
  rescheduleTime = '';
  rescheduleReason = '';
  rescheduleNotes = '';
  availableSlots: TimeSlot[] = [];
  isLoadingSlots = false;
  isRescheduling = false;

  constructor(
    private appointmentService: AppointmentService,
    private doctorService: DoctorService,
    private patientService: PatientService,
    public authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDoctors();
    this.loadPatients();

    this.route.queryParams.subscribe(params => {
      if (params['filter']) {
        const f = params['filter'];
        if (f === 'today' || f === 'upcoming' || f === 'history' || f === 'all') {
          this.timeFilter = f;
        }
        if (f === 'today') {
          this.selectedDate = this.getTodayDate();
        } else {
          this.selectedDate = '';
        }
      }
      if (params['status']) {
        this.selectedStatus = params['status'] as AppointmentStatus;
      }
      if (params['date']) {
        this.selectedDate = params['date'];
        if (this.selectedDate === this.getTodayDate()) {
          this.timeFilter = 'today';
        }
      }
      if (params['doctorId']) {
        this.selectedDoctorId = Number(params['doctorId']);
      }
      if (params['patientId']) {
        this.selectedPatientId = Number(params['patientId']);
      }
      if (params['search']) {
        this.searchTerm = params['search'];
      }
      this.loadAppointments();
    });
  }

  getTodayDate(): string {
    const d = new Date();
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  loadPatients(): void {
    if (!this.isPatient) {
      this.patientService.getAll().subscribe({
        next: (res) => {
          this.patients = res.data || [];
        },
        error: () => {}
      });
    }
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

  get canConfirm(): boolean {
    return this.authService.hasAnyRole(['ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_DOCTOR']);
  }

  get canComplete(): boolean {
    return this.authService.hasAnyRole(['ROLE_ADMIN', 'ROLE_DOCTOR']);
  }

  loadDoctors(): void {
    this.doctorService.getAll().subscribe({
      next: (res) => {
        this.doctors = res.data || [];
      },
      error: () => {}
    });
  }

  loadAppointments(): void {
    this.isLoading = true;
    this.errorMessage = null;

    const filters: any = {};
    if (this.searchTerm) filters.search = this.searchTerm;
    if (this.selectedStatus) filters.status = this.selectedStatus;

    if (this.selectedDate) {
      filters.date = this.selectedDate;
    } else if (this.timeFilter === 'today') {
      filters.date = this.getTodayDate();
    }

    if (this.selectedDoctorId) filters.doctorId = Number(this.selectedDoctorId);
    if (this.selectedPatientId) filters.patientId = Number(this.selectedPatientId);

    // If logged in as Doctor, auto-filter to their doctor ID if known
    if (this.isDoctor && !this.selectedDoctorId) {
      const user = this.authService.currentUserValue;
      if (user?.profileId) {
        filters.doctorId = user.profileId;
      }
    }

    // If logged in as Patient, auto-filter to their patient ID if known
    if (this.isPatient) {
      const user = this.authService.currentUserValue;
      if (user?.profileId) {
        filters.patientId = user.profileId;
      }
    }

    this.appointmentService.getAll(filters).subscribe({
      next: (res) => {
        let list = res.data || [];
        const today = this.getTodayDate();

        if (this.timeFilter === 'upcoming') {
          list = list
            .filter(a => a.appointmentDate >= today && a.status !== 'CANCELLED' && a.status !== 'COMPLETED')
            .sort((a, b) => (a.appointmentDate + ' ' + (a.appointmentTime || '')).localeCompare(b.appointmentDate + ' ' + (b.appointmentTime || '')));
        } else if (this.timeFilter === 'history') {
          list = list
            .filter(a => a.appointmentDate < today || a.status === 'COMPLETED' || a.status === 'CANCELLED')
            .sort((a, b) => (b.appointmentDate + ' ' + (b.appointmentTime || '')).localeCompare(a.appointmentDate + ' ' + (a.appointmentTime || '')));
        }

        this.appointments = list;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load appointments.';
        this.isLoading = false;
      }
    });
  }

  setTimeFilter(filter: 'all' | 'today' | 'upcoming' | 'history'): void {
    this.timeFilter = filter;
    if (filter === 'today') {
      this.selectedDate = this.getTodayDate();
    } else {
      this.selectedDate = '';
    }
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { filter: filter === 'all' ? null : filter, date: null },
      queryParamsHandling: 'merge'
    });
    this.loadAppointments();
  }

  setStatusFilter(status: AppointmentStatus | ''): void {
    this.selectedStatus = status;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { status: status ? status : null },
      queryParamsHandling: 'merge'
    });
    this.loadAppointments();
  }

  get isAnyFilterActive(): boolean {
    return this.timeFilter !== 'all' || !!this.selectedStatus || !!this.selectedDate || !!this.searchTerm || !!this.selectedDoctorId || !!this.selectedPatientId;
  }

  get activeFilterTitle(): string {
    const parts: string[] = [];
    if (this.timeFilter === 'today') parts.push(`Today's Consultations (${this.getTodayDate()})`);
    else if (this.timeFilter === 'upcoming') parts.push('Upcoming Consultations');
    else if (this.timeFilter === 'history') parts.push('Appointment History / Past Visits');

    if (this.selectedStatus) parts.push(`Status: ${this.selectedStatus}`);
    if (this.selectedDate && this.timeFilter !== 'today') parts.push(`Date: ${this.selectedDate}`);
    if (this.searchTerm) parts.push(`Keyword: "${this.searchTerm}"`);
    if (this.selectedDoctorId) {
      const doc = this.doctors.find(d => d.doctorId === Number(this.selectedDoctorId) || (d as any).id === Number(this.selectedDoctorId));
      if (doc) parts.push(`Dr. ${doc.name || (doc as any).doctorName}`);
      else if (this.isDoctor && this.selectedDoctorId === this.authService.currentUserValue?.profileId) parts.push('My Consultations');
      else parts.push(`Doctor #${this.selectedDoctorId}`);
    }
    if (this.selectedPatientId) {
      const pat = this.patients.find(p => p.id === Number(this.selectedPatientId) || (p as any).patientId === Number(this.selectedPatientId));
      if (pat) parts.push(`Patient: ${pat.name || (pat as any).patientName || ''}`);
      else if (this.isPatient && this.selectedPatientId === this.authService.currentUserValue?.profileId) parts.push('My Appointments');
      else parts.push(`Patient #${this.selectedPatientId}`);
    }
    return parts.join(' | ') || 'All Appointments';
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = '';
    this.selectedDate = '';
    this.selectedDoctorId = '';
    this.selectedPatientId = '';
    this.timeFilter = 'all';
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {}
    });
    this.loadAppointments();
  }

  // Quick Action: Confirm
  onConfirm(app: Appointment): void {
    if (!app.id) return;
    this.appointmentService.confirm(app.id).subscribe({
      next: (res) => {
        this.showSuccess(`Appointment #${app.id} confirmed successfully.`);
        this.loadAppointments();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to confirm appointment.';
      }
    });
  }

  // Quick Action: Complete
  onComplete(app: Appointment): void {
    if (!app.id) return;
    this.appointmentService.complete(app.id).subscribe({
      next: (res) => {
        this.showSuccess(`Appointment #${app.id} marked as completed.`);
        this.loadAppointments();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to complete appointment.';
      }
    });
  }

  // Modal: Open Cancel
  openCancelModal(app: Appointment): void {
    this.cancelTarget = app;
    this.cancelReason = '';
    this.errorMessage = null;
  }

  closeCancelModal(): void {
    this.cancelTarget = null;
    this.cancelReason = '';
  }

  confirmCancel(): void {
    if (!this.cancelTarget?.id) return;
    this.isCancelling = true;

    this.appointmentService.cancel(this.cancelTarget.id, this.cancelReason).subscribe({
      next: () => {
        this.showSuccess(`Appointment #${this.cancelTarget?.id} has been cancelled.`);
        this.isCancelling = false;
        this.closeCancelModal();
        this.loadAppointments();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to cancel appointment.';
        this.isCancelling = false;
      }
    });
  }

  // Modal: Open Reschedule
  openRescheduleModal(app: Appointment): void {
    this.rescheduleTarget = app;
    this.rescheduleDate = '';
    this.rescheduleTime = '';
    this.rescheduleReason = '';
    this.rescheduleNotes = '';
    this.availableSlots = [];
    this.errorMessage = null;
  }

  closeRescheduleModal(): void {
    this.rescheduleTarget = null;
    this.availableSlots = [];
  }

  onRescheduleDateChange(): void {
    if (!this.rescheduleTarget || !this.rescheduleDate) {
      this.availableSlots = [];
      return;
    }

    this.isLoadingSlots = true;
    this.rescheduleTime = '';
    this.appointmentService.getAvailableSlots(this.rescheduleTarget.doctorId, this.rescheduleDate).subscribe({
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
      this.rescheduleTime = slot.time;
    }
  }

  confirmReschedule(): void {
    if (!this.rescheduleTarget?.id || !this.rescheduleDate || !this.rescheduleTime) {
      this.errorMessage = 'Please select both a date and an available time slot.';
      return;
    }

    this.isRescheduling = true;
    this.appointmentService.reschedule(this.rescheduleTarget.id, {
      appointmentDate: this.rescheduleDate,
      appointmentTime: this.rescheduleTime,
      reason: this.rescheduleReason,
      notes: this.rescheduleNotes
    }).subscribe({
      next: () => {
        this.showSuccess(`Appointment #${this.rescheduleTarget?.id} rescheduled successfully.`);
        this.isRescheduling = false;
        this.closeRescheduleModal();
        this.loadAppointments();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to reschedule appointment.';
        this.isRescheduling = false;
      }
    });
  }

  getStatusBadgeClass(status: AppointmentStatus): string {
    switch (status) {
      case 'BOOKED': return 'bg-warning text-dark';
      case 'CONFIRMED': return 'bg-primary';
      case 'COMPLETED': return 'bg-success';
      case 'CANCELLED': return 'bg-danger';
      case 'RESCHEDULED': return 'bg-info text-dark';
      default: return 'bg-secondary';
    }
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => {
      this.successMessage = null;
    }, 4000);
  }
}
