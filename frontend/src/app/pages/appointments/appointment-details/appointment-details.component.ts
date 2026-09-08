import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AuthService } from '../../../core/services/auth.service';
import { Appointment, AppointmentStatus, TimeSlot } from '../../../models/appointment.model';

@Component({
  selector: 'app-appointment-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './appointment-details.component.html',
  styleUrls: ['./appointment-details.component.css']
})
export class AppointmentDetailsComponent implements OnInit {
  appointmentId!: number;
  appointment: Appointment | null = null;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Cancel state
  showCancelModal = false;
  cancelReason = '';
  isCancelling = false;

  // Reschedule state
  showRescheduleModal = false;
  rescheduleDate = '';
  rescheduleTime = '';
  rescheduleReason = '';
  rescheduleNotes = '';
  availableSlots: TimeSlot[] = [];
  isLoadingSlots = false;
  isRescheduling = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private appointmentService: AppointmentService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.appointmentId = Number(idParam);
      this.loadAppointment();
    } else {
      this.router.navigate(['/appointments']);
    }
  }

  get canConfirm(): boolean {
    return this.authService.hasAnyRole(['ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_DOCTOR']);
  }

  get canComplete(): boolean {
    return this.authService.hasAnyRole(['ROLE_ADMIN', 'ROLE_DOCTOR']);
  }

  get isDoctorOrAdmin(): boolean {
    return this.authService.hasAnyRole(['ROLE_ADMIN', 'ROLE_DOCTOR']);
  }

  get canManageBilling(): boolean {
    return this.authService.hasAnyRole(['ROLE_ADMIN', 'ROLE_RECEPTIONIST']);
  }

  loadAppointment(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.appointmentService.getById(this.appointmentId).subscribe({
      next: (res) => {
        this.appointment = res.data || null;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load appointment details.';
        this.isLoading = false;
      }
    });
  }

  // Quick Action: Confirm
  confirmAppointment(): void {
    if (!this.appointment?.id) return;
    this.appointmentService.confirm(this.appointment.id).subscribe({
      next: (res) => {
        this.appointment = res.data || null;
        this.showSuccess('Appointment confirmed successfully.');
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to confirm appointment.';
      }
    });
  }

  // Quick Action: Complete
  completeAppointment(): void {
    if (!this.appointment?.id) return;
    this.appointmentService.complete(this.appointment.id).subscribe({
      next: (res) => {
        this.appointment = res.data || null;
        this.showSuccess('Appointment marked as completed.');
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to mark appointment completed.';
      }
    });
  }

  // Cancel Modal Handlers
  openCancelModal(): void {
    this.cancelReason = '';
    this.showCancelModal = true;
  }

  closeCancelModal(): void {
    this.showCancelModal = false;
  }

  confirmCancel(): void {
    if (!this.appointment?.id) return;
    this.isCancelling = true;

    this.appointmentService.cancel(this.appointment.id, this.cancelReason).subscribe({
      next: (res) => {
        this.appointment = res.data || null;
        this.isCancelling = false;
        this.closeCancelModal();
        this.showSuccess('Appointment has been cancelled.');
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to cancel appointment.';
        this.isCancelling = false;
      }
    });
  }

  // Reschedule Modal Handlers
  openRescheduleModal(): void {
    this.rescheduleDate = '';
    this.rescheduleTime = '';
    this.rescheduleReason = '';
    this.rescheduleNotes = '';
    this.availableSlots = [];
    this.showRescheduleModal = true;
  }

  closeRescheduleModal(): void {
    this.showRescheduleModal = false;
    this.availableSlots = [];
  }

  onRescheduleDateChange(): void {
    if (!this.appointment || !this.rescheduleDate) {
      this.availableSlots = [];
      return;
    }

    this.isLoadingSlots = true;
    this.rescheduleTime = '';
    this.appointmentService.getAvailableSlots(this.appointment.doctorId, this.rescheduleDate).subscribe({
      next: (res) => {
        this.availableSlots = res.data || [];
        this.isLoadingSlots = false;
      },
      error: () => {
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
    if (!this.appointment?.id || !this.rescheduleDate || !this.rescheduleTime) {
      this.errorMessage = 'Please select a date and an available time slot.';
      return;
    }

    this.isRescheduling = true;
    this.appointmentService.reschedule(this.appointment.id, {
      appointmentDate: this.rescheduleDate,
      appointmentTime: this.rescheduleTime,
      reason: this.rescheduleReason,
      notes: this.rescheduleNotes
    }).subscribe({
      next: (res) => {
        this.appointment = res.data || null;
        this.isRescheduling = false;
        this.closeRescheduleModal();
        this.showSuccess('Appointment rescheduled successfully.');
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to reschedule appointment.';
        this.isRescheduling = false;
      }
    });
  }

  printDetails(): void {
    window.print();
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
