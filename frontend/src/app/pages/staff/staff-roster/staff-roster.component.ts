import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { StaffRosterService } from '../../../core/services/staff-roster.service';
import { AuthService } from '../../../core/services/auth.service';
import { StaffShift } from '../../../models/staff-roster.model';

@Component({
  selector: 'app-staff-roster',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './staff-roster.component.html',
  styleUrls: ['./staff-roster.component.css']
})
export class StaffRosterComponent implements OnInit {
  shifts: StaffShift[] = [];
  onCallSpecialists: StaffShift[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  selectedDay: string = 'MONDAY';
  selectedDepartment: string = 'ALL';

  daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  // Add Shift Modal
  showAddModal = false;
  isSubmitting = false;
  newShift: StaffShift = {
    staffName: '',
    roleTitle: 'Attending Physician',
    department: 'Emergency & Trauma',
    dayOfWeek: 'MONDAY',
    shiftType: 'MORNING',
    startTime: '07:00',
    endTime: '15:00',
    onCall: false,
    contactPhone: '+1-555-0199'
  };

  constructor(
    public staffRosterService: StaffRosterService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadShifts();
  }

  loadShifts(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.staffRosterService.getAll().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.shifts = res.data;
          this.onCallSpecialists = this.shifts.filter(s => s.onCall);
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load duty roster.';
        this.isLoading = false;
      }
    });
  }

  get filteredShifts(): StaffShift[] {
    return this.shifts.filter(s => {
      const matchDay = this.selectedDay === 'ALL' || s.dayOfWeek === this.selectedDay;
      const matchDept = this.selectedDepartment === 'ALL' || s.department === this.selectedDepartment;
      return matchDay && matchDept;
    });
  }

  getShiftsByType(shiftType: 'MORNING' | 'EVENING' | 'NIGHT'): StaffShift[] {
    return this.filteredShifts.filter(s => s.shiftType === shiftType);
  }

  get onCallCount(): number {
    return this.shifts.filter(s => s.onCall).length;
  }

  get morningCount(): number {
    return this.shifts.filter(s => s.shiftType === 'MORNING' && (this.selectedDay === 'ALL' || s.dayOfWeek === this.selectedDay)).length;
  }

  get eveningCount(): number {
    return this.shifts.filter(s => s.shiftType === 'EVENING' && (this.selectedDay === 'ALL' || s.dayOfWeek === this.selectedDay)).length;
  }

  get nightCount(): number {
    return this.shifts.filter(s => s.shiftType === 'NIGHT' && (this.selectedDay === 'ALL' || s.dayOfWeek === this.selectedDay)).length;
  }

  toggleOnCall(shift: StaffShift): void {
    if (!shift.id) return;
    const nextVal = !shift.onCall;
    this.staffRosterService.toggleOnCall(shift.id, nextVal).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.shifts.findIndex(x => x.id === shift.id);
          if (idx !== -1) this.shifts[idx] = res.data;
          this.onCallSpecialists = this.shifts.filter(s => s.onCall);
          this.successMessage = `${res.data.staffName} is ${nextVal ? 'now ON-CALL' : 'no longer on-call'}`;
          setTimeout(() => this.successMessage = null, 3000);
        }
      }
    });
  }

  openAddModal(): void {
    this.newShift = {
      staffName: '',
      roleTitle: 'Specialist Physician',
      department: 'Emergency & Trauma',
      dayOfWeek: this.selectedDay === 'ALL' ? 'MONDAY' : this.selectedDay,
      shiftType: 'MORNING',
      startTime: '07:00',
      endTime: '15:00',
      onCall: false,
      contactPhone: '+1-555-0199'
    };
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
  }

  onShiftTypeChange(): void {
    if (this.newShift.shiftType === 'MORNING') {
      this.newShift.startTime = '07:00';
      this.newShift.endTime = '15:00';
    } else if (this.newShift.shiftType === 'EVENING') {
      this.newShift.startTime = '15:00';
      this.newShift.endTime = '23:00';
    } else if (this.newShift.shiftType === 'NIGHT') {
      this.newShift.startTime = '23:00';
      this.newShift.endTime = '07:00';
    }
  }

  saveShift(): void {
    if (!this.newShift.staffName) {
      alert('Please enter staff name.');
      return;
    }
    this.isSubmitting = true;
    this.staffRosterService.saveShift(this.newShift).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        if (res.success && res.data) {
          this.shifts.unshift(res.data);
          this.onCallSpecialists = this.shifts.filter(s => s.onCall);
          this.successMessage = `Shift scheduled for ${res.data.staffName} (${res.data.dayOfWeek})`;
          setTimeout(() => this.successMessage = null, 3500);
          this.closeAddModal();
        }
      },
      error: (err) => {
        this.isSubmitting = false;
        alert('Failed to schedule shift: ' + (err.error?.message || err.message));
      }
    });
  }
}
