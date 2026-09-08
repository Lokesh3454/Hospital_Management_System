import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SurgeryService } from '../../../core/services/surgery.service';
import { AuthService } from '../../../core/services/auth.service';
import { SurgerySchedule } from '../../../models/surgery.model';

@Component({
  selector: 'app-surgery-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './surgery-management.component.html',
  styleUrls: ['./surgery-management.component.css']
})
export class SurgeryManagementComponent implements OnInit {
  schedules: SurgerySchedule[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  selectedRoom: string = 'ALL';
  selectedStatus: string = 'ALL';

  // Schedule Surgery Modal
  showScheduleModal = false;
  isSubmitting = false;
  newSurgery: SurgerySchedule = {
    surgeryNumber: '',
    patientName: '',
    leadSurgeonName: '',
    anesthesiologistName: '',
    scrubNurseName: '',
    otRoom: 'OT-1 (General Surgery)',
    procedureName: '',
    surgeryDate: new Date().toISOString().split('T')[0],
    scheduledStartTime: '09:00',
    estimatedDurationHours: 2.5,
    status: 'SCHEDULED',
    preOpCleared: false,
    anesthesiaCleared: false,
    consentSigned: false,
    bloodReserved: false
  };

  // Status & PACU update modal
  selectedSurgeryForStatus: SurgerySchedule | null = null;
  showStatusModal = false;
  targetStatus: 'SCHEDULED' | 'IN_PROGRESS' | 'IN_PACU' | 'COMPLETED' | 'CANCELLED' = 'IN_PROGRESS';
  pacuScore: number = 8;

  constructor(
    public surgeryService: SurgeryService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadSchedules();
  }

  loadSchedules(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.surgeryService.getAll().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.schedules = res.data;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load surgery schedules.';
        this.isLoading = false;
      }
    });
  }

  get filteredSchedules(): SurgerySchedule[] {
    return this.schedules.filter(s => {
      const matchRoom = this.selectedRoom === 'ALL' || s.otRoom.includes(this.selectedRoom);
      const matchStatus = this.selectedStatus === 'ALL' || s.status === this.selectedStatus;
      return matchRoom && matchStatus;
    });
  }

  get inProgressCount(): number {
    return this.schedules.filter(s => s.status === 'IN_PROGRESS').length;
  }

  get inPacuCount(): number {
    return this.schedules.filter(s => s.status === 'IN_PACU').length;
  }

  get scheduledCount(): number {
    return this.schedules.filter(s => s.status === 'SCHEDULED').length;
  }

  get clearancePendingCount(): number {
    return this.schedules.filter(s => s.status === 'SCHEDULED' && (!s.preOpCleared || !s.anesthesiaCleared || !s.consentSigned)).length;
  }

  openScheduleModal(): void {
    const randomSuffix = Math.floor(1000 + Math.random() * 9000);
    this.newSurgery = {
      surgeryNumber: `SURG-${randomSuffix}`,
      patientName: '',
      leadSurgeonName: '',
      anesthesiologistName: '',
      scrubNurseName: '',
      otRoom: 'OT-1 (General Surgery)',
      procedureName: '',
      surgeryDate: new Date().toISOString().split('T')[0],
      scheduledStartTime: '09:00',
      estimatedDurationHours: 2.0,
      status: 'SCHEDULED',
      preOpCleared: true,
      anesthesiaCleared: false,
      consentSigned: false,
      bloodReserved: false
    };
    this.showScheduleModal = true;
  }

  closeScheduleModal(): void {
    this.showScheduleModal = false;
  }

  saveSchedule(): void {
    if (!this.newSurgery.patientName || !this.newSurgery.procedureName || !this.newSurgery.leadSurgeonName) {
      alert('Please fill in patient name, surgical procedure, and lead surgeon.');
      return;
    }
    this.isSubmitting = true;
    this.surgeryService.scheduleSurgery(this.newSurgery).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        if (res.success && res.data) {
          this.schedules.unshift(res.data);
          this.successMessage = `Surgery ${res.data.surgeryNumber} scheduled for ${res.data.patientName}!`;
          setTimeout(() => this.successMessage = null, 4000);
          this.closeScheduleModal();
        }
      },
      error: (err) => {
        this.isSubmitting = false;
        alert('Failed to schedule surgery: ' + (err.error?.message || err.message));
      }
    });
  }

  toggleChecklist(s: SurgerySchedule, item: 'preOpCleared' | 'anesthesiaCleared' | 'consentSigned' | 'bloodReserved'): void {
    if (!s.id) return;
    const updateObj: any = {};
    updateObj[item] = !s[item];
    this.surgeryService.updateClearance(s.id, updateObj).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.schedules.findIndex(x => x.id === s.id);
          if (idx !== -1) this.schedules[idx] = res.data;
        }
      }
    });
  }

  openStatusModal(s: SurgerySchedule): void {
    this.selectedSurgeryForStatus = s;
    this.targetStatus = s.status;
    this.pacuScore = s.pacuRecoveryScore || 8;
    this.showStatusModal = true;
  }

  closeStatusModal(): void {
    this.showStatusModal = false;
    this.selectedSurgeryForStatus = null;
  }

  updateSurgeryStatus(): void {
    if (!this.selectedSurgeryForStatus || !this.selectedSurgeryForStatus.id) return;
    const score = this.targetStatus === 'IN_PACU' || this.targetStatus === 'COMPLETED' ? this.pacuScore : undefined;
    this.surgeryService.updateStatus(this.selectedSurgeryForStatus.id, this.targetStatus, score).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.schedules.findIndex(x => x.id === this.selectedSurgeryForStatus!.id);
          if (idx !== -1) this.schedules[idx] = res.data;
          this.successMessage = `Operating Suite updated: ${res.data.status}`;
          setTimeout(() => this.successMessage = null, 3500);
        }
        this.closeStatusModal();
      },
      error: (err) => {
        alert('Failed to update status: ' + (err.error?.message || err.message));
      }
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'IN_PROGRESS': return 'badge bg-danger pulse-badge';
      case 'IN_PACU': return 'badge bg-warning text-dark';
      case 'SCHEDULED': return 'badge bg-primary';
      case 'COMPLETED': return 'badge bg-success';
      case 'CANCELLED': return 'badge bg-secondary';
      default: return 'badge bg-light text-dark';
    }
  }
}
