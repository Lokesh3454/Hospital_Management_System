import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { BedService } from '../../../core/services/bed.service';
import { PatientService } from '../../../core/services/patient.service';
import { VitalsService } from '../../../core/services/vitals.service';
import { AuthService } from '../../../core/services/auth.service';
import { Bed, WardType, BedStatus } from '../../../models/bed.model';
import { Patient } from '../../../models/patient.model';
import { Vitals } from '../../../models/vitals.model';

@Component({
  selector: 'app-bed-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './bed-management.component.html',
  styleUrls: ['./bed-management.component.css']
})
export class BedManagementComponent implements OnInit {
  beds: Bed[] = [];
  patients: Patient[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  selectedWard: string = 'ALL';
  selectedStatus: string = 'ALL';

  // Bed Details Modal (Card Click Feature)
  showBedDetailsModal = false;
  selectedBedForDetails: Bed | null = null;
  stayDurationDays = 0;
  stayDurationHours = 0;
  accruedRoomCharges = 0;
  latestVitals: Vitals | null = null;
  isLoadingVitals = false;

  // In-modal Quick Vitals Recording
  showInlineVitalsForm = false;
  newVitals: Vitals = {
    patientId: 0,
    systolicBP: 120,
    diastolicBP: 80,
    heartRate: 75,
    spo2: 98,
    temperature: 98.6,
    respiratoryRate: 16,
    notes: 'Routine inpatient bedside check'
  };
  isSubmittingVitals = false;

  // In-modal Bed Transfer State
  showTransferModal = false;
  transferTargetBedId: number | null = null;
  isSubmittingTransfer = false;

  // In-modal Sanitation Checklist State
  sanitationStep1 = false;
  sanitationStep2 = false;
  sanitationStep3 = false;
  sanitationStep4 = false;

  // Admit Modal State
  showAdmitModal = false;
  selectedBedForAdmit: Bed | null = null;
  admitPatientId: number | null = null;
  admitNotes: string = '';
  isSubmittingAdmit = false;

  // New Bed Modal State
  showNewBedModal = false;
  newBed: Bed = {
    bedNumber: '',
    wardType: 'GENERAL',
    roomNumber: '',
    status: 'AVAILABLE',
    dailyRate: 1200
  };
  isSubmittingNewBed = false;

  constructor(
    private bedService: BedService,
    private patientService: PatientService,
    private vitalsService: VitalsService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadBeds();
    this.loadPatients();
  }

  loadBeds(): void {
    this.isLoading = true;
    this.bedService.getAllBeds().subscribe({
      next: (res) => {
        this.beds = res.data || [];
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load beds: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  loadPatients(): void {
    this.patientService.getAll().subscribe({
      next: (res) => {
        this.patients = res.data || [];
      },
      error: () => {}
    });
  }

  // KPI Quick Filter Toggle
  filterByStatus(status: string): void {
    if (this.selectedStatus === status && status !== 'ALL') {
      this.selectedStatus = 'ALL';
    } else {
      this.selectedStatus = status;
    }
  }

  get filteredBeds(): Bed[] {
    return this.beds.filter(b => {
      const matchWard = this.selectedWard === 'ALL' || b.wardType === this.selectedWard;
      const matchStatus = this.selectedStatus === 'ALL' || b.status === this.selectedStatus;
      return matchWard && matchStatus;
    });
  }

  get availableTransferBeds(): Bed[] {
    return this.beds.filter(b => b.status === 'AVAILABLE' && b.id !== this.selectedBedForDetails?.id);
  }

  get totalCount(): number { return this.beds.length; }
  get availableCount(): number { return this.beds.filter(b => b.status === 'AVAILABLE').length; }
  get occupiedCount(): number { return this.beds.filter(b => b.status === 'OCCUPIED').length; }
  get cleaningCount(): number { return this.beds.filter(b => b.status === 'CLEANING').length; }
  get occupancyRate(): number {
    return this.totalCount > 0 ? Math.round((this.occupiedCount / this.totalCount) * 100) : 0;
  }

  // ==========================================
  // CARD CLICK FEATURE: Bed Details Modal
  // ==========================================
  openBedDetails(bed: Bed): void {
    this.selectedBedForDetails = bed;
    this.showBedDetailsModal = true;
    this.showInlineVitalsForm = false;
    this.showTransferModal = false;
    this.sanitationStep1 = false;
    this.sanitationStep2 = false;
    this.sanitationStep3 = false;
    this.sanitationStep4 = false;
    this.latestVitals = null;

    if (bed.status === 'OCCUPIED' && bed.admissionDate) {
      const admitTime = new Date(bed.admissionDate).getTime();
      const now = new Date().getTime();
      const diffMs = Math.max(0, now - admitTime);
      const totalHours = Math.floor(diffMs / (1000 * 60 * 60));
      this.stayDurationDays = Math.floor(totalHours / 24);
      this.stayDurationHours = totalHours % 24;

      // Charge calculation: at least 1 day or days + 1
      const billableDays = Math.max(1, this.stayDurationDays + (this.stayDurationHours > 0 ? 1 : 0));
      this.accruedRoomCharges = billableDays * (bed.dailyRate || 0);

      // Load patient's latest vitals
      if (bed.patientId) {
        this.isLoadingVitals = true;
        this.vitalsService.getLatestVitals(bed.patientId).subscribe({
          next: (res) => {
            this.latestVitals = res.data || null;
            this.isLoadingVitals = false;
          },
          error: () => {
            this.isLoadingVitals = false;
          }
        });
      }
    }
  }

  closeBedDetailsModal(): void {
    this.showBedDetailsModal = false;
    this.selectedBedForDetails = null;
    this.showInlineVitalsForm = false;
    this.showTransferModal = false;
  }

  // In-modal Quick Vitals Recording
  toggleInlineVitalsForm(): void {
    this.showInlineVitalsForm = !this.showInlineVitalsForm;
    if (this.showInlineVitalsForm && this.selectedBedForDetails?.patientId) {
      this.newVitals = {
        patientId: this.selectedBedForDetails.patientId,
        systolicBP: 120,
        diastolicBP: 80,
        heartRate: 74,
        spo2: 98,
        temperature: 98.6,
        respiratoryRate: 16,
        notes: `Inpatient check at Bed ${this.selectedBedForDetails.bedNumber}`
      };
    }
  }

  saveBedVitals(): void {
    if (!this.selectedBedForDetails?.patientId) return;
    this.isSubmittingVitals = true;
    this.vitalsService.recordVitals(this.newVitals).subscribe({
      next: (res) => {
        this.isSubmittingVitals = false;
        this.showInlineVitalsForm = false;
        this.latestVitals = res.data || null;
        this.successMessage = `Vitals logged for ${this.selectedBedForDetails?.patientName || 'patient'} at Bed ${this.selectedBedForDetails?.bedNumber}.`;
        setTimeout(() => this.successMessage = null, 4000);
      },
      error: (err) => {
        this.errorMessage = 'Failed to record vitals: ' + (err.error?.message || err.message);
        this.isSubmittingVitals = false;
      }
    });
  }

  // In-modal Bed Transfer
  toggleTransferModal(): void {
    this.showTransferModal = !this.showTransferModal;
    this.transferTargetBedId = null;
  }

  confirmBedTransfer(): void {
    if (!this.selectedBedForDetails || !this.selectedBedForDetails.id || !this.selectedBedForDetails.patientId || !this.transferTargetBedId) return;

    this.isSubmittingTransfer = true;
    const patientId = this.selectedBedForDetails.patientId;
    const oldBedId = this.selectedBedForDetails.id;
    const oldBedNumber = this.selectedBedForDetails.bedNumber;
    const targetBed = this.beds.find(b => b.id === this.transferTargetBedId);

    // 1. Assign to new bed
    this.bedService.assignPatient(this.transferTargetBedId, patientId, `Transferred from ${oldBedNumber}`).subscribe({
      next: () => {
        // 2. Discharge old bed to CLEANING
        this.bedService.dischargePatient(oldBedId).subscribe({
          next: () => {
            this.isSubmittingTransfer = false;
            this.closeBedDetailsModal();
            this.successMessage = `Patient successfully transferred from ${oldBedNumber} to ${targetBed?.bedNumber}. Old bed set to CLEANING.`;
            this.loadBeds();
            setTimeout(() => this.successMessage = null, 4500);
          },
          error: (err) => {
            this.errorMessage = 'Transfer error vacating previous bed: ' + (err.error?.message || err.message);
            this.isSubmittingTransfer = false;
          }
        });
      },
      error: (err) => {
        this.errorMessage = 'Transfer error assigning to target bed: ' + (err.error?.message || err.message);
        this.isSubmittingTransfer = false;
      }
    });
  }

  // In-modal Sanitation Checklist Complete
  get canCompleteSanitation(): boolean {
    return this.sanitationStep1 && this.sanitationStep2 && this.sanitationStep3 && this.sanitationStep4;
  }

  completeSanitation(): void {
    if (!this.selectedBedForDetails) return;
    this.markBedReady(this.selectedBedForDetails);
    this.closeBedDetailsModal();
  }

  // Navigate to Patient Profile
  navigateToPatient(patientId?: number): void {
    if (!patientId) return;
    this.closeBedDetailsModal();
    this.router.navigate(['/patients', patientId]);
  }

  // Order Lab Test for Inpatient
  orderLabForInpatient(): void {
    this.closeBedDetailsModal();
    this.router.navigate(['/lab']);
  }

  // ==========================================
  // ADMIT MODAL
  // ==========================================
  openAdmitModal(bed: Bed): void {
    this.selectedBedForAdmit = bed;
    this.admitPatientId = null;
    this.admitNotes = '';
    this.showAdmitModal = true;
    if (this.showBedDetailsModal) {
      this.closeBedDetailsModal();
    }
  }

  closeAdmitModal(): void {
    this.showAdmitModal = false;
    this.selectedBedForAdmit = null;
  }

  confirmAdmit(): void {
    if (!this.selectedBedForAdmit || !this.selectedBedForAdmit.id || !this.admitPatientId) return;

    this.isSubmittingAdmit = true;
    this.bedService.assignPatient(this.selectedBedForAdmit.id, this.admitPatientId, this.admitNotes).subscribe({
      next: () => {
        this.isSubmittingAdmit = false;
        this.closeAdmitModal();
        this.successMessage = `Patient admitted to bed ${this.selectedBedForAdmit?.bedNumber} successfully.`;
        this.loadBeds();
        setTimeout(() => this.successMessage = null, 4000);
      },
      error: (err) => {
        this.errorMessage = 'Admission failed: ' + (err.error?.message || err.message);
        this.isSubmittingAdmit = false;
      }
    });
  }

  dischargePatient(bed: Bed): void {
    if (!bed.id) return;
    this.bedService.dischargePatient(bed.id).subscribe({
      next: () => {
        if (this.showBedDetailsModal) {
          this.closeBedDetailsModal();
        }
        this.successMessage = `Bed ${bed.bedNumber} vacated and set for sanitation/cleaning.`;
        this.loadBeds();
        setTimeout(() => this.successMessage = null, 4000);
      },
      error: (err) => {
        this.errorMessage = 'Discharge failed: ' + (err.error?.message || err.message);
      }
    });
  }

  markBedReady(bed: Bed): void {
    if (!bed.id) return;
    bed.status = 'AVAILABLE';
    this.bedService.updateBed(bed.id, bed).subscribe({
      next: () => {
        this.successMessage = `Bed ${bed.bedNumber} sanitized and marked AVAILABLE for admission.`;
        this.loadBeds();
        setTimeout(() => this.successMessage = null, 4000);
      },
      error: (err) => {
        this.errorMessage = 'Status update failed: ' + (err.error?.message || err.message);
      }
    });
  }

  openNewBedModal(): void {
    this.newBed = {
      bedNumber: 'GEN-' + (200 + this.beds.length + 1),
      wardType: 'GENERAL',
      roomNumber: 'Ward Block 2',
      status: 'AVAILABLE',
      dailyRate: 1200
    };
    this.showNewBedModal = true;
  }

  closeNewBedModal(): void {
    this.showNewBedModal = false;
  }

  saveNewBed(): void {
    if (!this.newBed.bedNumber) return;
    this.isSubmittingNewBed = true;
    this.bedService.createBed(this.newBed).subscribe({
      next: () => {
        this.isSubmittingNewBed = false;
        this.closeNewBedModal();
        this.successMessage = `Bed ${this.newBed.bedNumber} created in inventory.`;
        this.loadBeds();
        setTimeout(() => this.successMessage = null, 4000);
      },
      error: (err) => {
        this.errorMessage = 'Failed to create bed: ' + (err.error?.message || err.message);
        this.isSubmittingNewBed = false;
      }
    });
  }
}
