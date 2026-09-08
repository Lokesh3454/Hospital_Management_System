import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { BedService } from '../../../core/services/bed.service';
import { MedicineService } from '../../../core/services/medicine.service';
import { LabTestService } from '../../../core/services/lab-test.service';
import { AnalyticsService } from '../../../core/services/analytics.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { EmergencyService } from '../../../core/services/emergency.service';
import { SurgeryService } from '../../../core/services/surgery.service';
import { BloodBankService } from '../../../core/services/blood-bank.service';
import { InsuranceService } from '../../../core/services/insurance.service';
import { StaffRosterService } from '../../../core/services/staff-roster.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Bed } from '../../../models/bed.model';
import { Medicine } from '../../../models/medicine.model';
import { LabTest } from '../../../models/lab-test.model';
import { HospitalAnalytics } from '../../../models/analytics.model';
import { Appointment } from '../../../models/appointment.model';
import { EmergencyCase } from '../../../models/emergency.model';
import { SurgerySchedule } from '../../../models/surgery.model';
import { BloodInventory } from '../../../models/blood-bank.model';
import { InsuranceClaim } from '../../../models/insurance.model';
import { StaffShift } from '../../../models/staff-roster.model';
import { NotificationLog } from '../../../models/notification.model';

@Component({
  selector: 'app-operations-hub',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './operations-hub.component.html',
  styleUrls: ['./operations-hub.component.css']
})
export class OperationsHubComponent implements OnInit, OnDestroy {
  isLoading = true;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Live domain telemetry
  beds: Bed[] = [];
  medicines: Medicine[] = [];
  labTests: LabTest[] = [];
  todayAppointments: Appointment[] = [];
  analytics: HospitalAnalytics | null = null;

  // Enterprise telemetry
  emergencyCases: EmergencyCase[] = [];
  surgeries: SurgerySchedule[] = [];
  bloodInventory: BloodInventory[] = [];
  claims: InsuranceClaim[] = [];
  shifts: StaffShift[] = [];
  notificationLogs: NotificationLog[] = [];
  onCallSpecialists: StaffShift[] = [];

  // Active Interactive Drawer/Modal
  activeModal: 'BEDS' | 'PHARMACY' | 'LAB' | 'QUEUE' | 'ANALYTICS' | 'TELEMED' | 'EMERGENCY' | 'SURGERY' | 'BLOOD_BANK' | 'INSURANCE' | 'ROSTER' | 'NOTIFICATIONS' | null = null;

  // Synthesizers
  audioCtx: any = null;
  isChimePlaying = false;
  isSirenPlaying = false;
  private sirenOsc: any = null;
  private sirenGain: any = null;

  // Quick Dispatch Outbox in hub
  quickAlertText = 'Hospital wide update: Emergency trauma bay and blood bank at normal operational readiness.';
  quickAlertChannel: 'SMS' | 'WHATSAPP' | 'EMAIL' = 'SMS';
  isDispatchingQuickAlert = false;

  constructor(
    public authService: AuthService,
    private router: Router,
    private bedService: BedService,
    private medicineService: MedicineService,
    private labTestService: LabTestService,
    private analyticsService: AnalyticsService,
    private appointmentService: AppointmentService,
    private emergencyService: EmergencyService,
    private surgeryService: SurgeryService,
    private bloodBankService: BloodBankService,
    private insuranceService: InsuranceService,
    private staffRosterService: StaffRosterService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadAllOperationsData();
  }

  ngOnDestroy(): void {
    this.stopSiren();
  }

  loadAllOperationsData(): void {
    this.isLoading = true;

    // Load Beds
    this.bedService.getAllBeds().subscribe({
      next: (res) => this.beds = res.data || [],
      error: () => {}
    });

    // Load Medicines
    this.medicineService.getAllMedicines().subscribe({
      next: (res) => this.medicines = res.data || [],
      error: () => {}
    });

    // Load Lab Tests
    this.labTestService.getAllLabTests().subscribe({
      next: (res) => this.labTests = res.data || [],
      error: () => {}
    });

    // Load Analytics
    this.analyticsService.getExecutiveAnalytics().subscribe({
      next: (res) => this.analytics = res.data || null,
      error: () => {}
    });

    // Load Emergency Cases
    this.emergencyService.getActive().subscribe({
      next: (res) => this.emergencyCases = res.data || [],
      error: () => {}
    });

    // Load Surgeries
    this.surgeryService.getAll().subscribe({
      next: (res) => this.surgeries = res.data || [],
      error: () => {}
    });

    // Load Blood Bank
    this.bloodBankService.getAll().subscribe({
      next: (res) => this.bloodInventory = res.data || [],
      error: () => {}
    });

    // Load Insurance
    this.insuranceService.getAll().subscribe({
      next: (res) => this.claims = res.data || [],
      error: () => {}
    });

    // Load Shifts
    this.staffRosterService.getAll().subscribe({
      next: (res) => {
        this.shifts = res.data || [];
        this.onCallSpecialists = this.shifts.filter(s => s.onCall);
      },
      error: () => {}
    });

    // Load Notifications
    this.notificationService.getRecent().subscribe({
      next: (res) => this.notificationLogs = res.data || [],
      error: () => {}
    });

    // Load Today's Outpatient Appointments for queue
    const today = new Date().toISOString().split('T')[0];
    this.appointmentService.getAll({ date: today }).subscribe({
      next: (res) => {
        this.todayAppointments = res.data || [];
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  // Enterprise Telemetry Getters
  get activeEmergencyCasesCount(): number {
    return this.emergencyCases.filter(c => c.status === 'TRIAGED' || c.status === 'IN_TREATMENT').length;
  }
  get isCodeBlueActive(): boolean {
    return this.emergencyCases.some(c => c.codeBlueTriggered);
  }
  get inProgressSurgeriesCount(): number {
    return this.surgeries.filter(s => s.status === 'IN_PROGRESS').length;
  }
  get totalBloodUnits(): number {
    return this.bloodInventory.reduce((sum, b) => sum + (b.unitsAvailable || 0), 0);
  }
  get criticalBloodShortages(): number {
    return this.bloodInventory.filter(b => b.unitsAvailable < 5).length;
  }
  get pendingClaimsCount(): number {
    return this.claims.filter(c => c.status === 'SUBMITTED' || c.status === 'UNDER_REVIEW').length;
  }
  get settledClaimsCount(): number {
    return this.claims.filter(c => c.status === 'SETTLED').length;
  }
  get resuscitationCasesCount(): number {
    return this.emergencyCases.filter(c => c.triageLevel === 'RESUSCITATION').length;
  }
  get inboundAmbulancesCount(): number {
    return this.emergencyCases.filter(c => (c.etaMinutes ?? 0) > 0).length;
  }
  get pacuSurgeriesCount(): number {
    return this.surgeries.filter(s => s.status === 'IN_PACU').length;
  }

  // Bed Telemetry Getters
  get totalBeds(): number { return this.beds.length; }
  get availableBeds(): number { return this.beds.filter(b => b.status === 'AVAILABLE').length; }
  get occupiedBeds(): number { return this.beds.filter(b => b.status === 'OCCUPIED').length; }
  get cleaningBeds(): number { return this.beds.filter(b => b.status === 'CLEANING').length; }
  get bedOccupancyRate(): number {
    return this.totalBeds > 0 ? Math.round((this.occupiedBeds / this.totalBeds) * 100) : 0;
  }

  // Pharmacy Telemetry Getters
  get totalMedicines(): number { return this.medicines.length; }
  get lowStockMedicines(): Medicine[] {
    return this.medicines.filter(m => m.stockQuantity <= m.reorderLevel);
  }
  get totalStockUnits(): number {
    return this.medicines.reduce((sum, m) => sum + (m.stockQuantity || 0), 0);
  }

  // Lab Telemetry Getters
  get totalLabOrders(): number { return this.labTests.length; }
  get pendingLabOrders(): number {
    return this.labTests.filter(t => t.status === 'ORDERED' || t.status === 'SAMPLE_COLLECTED' || t.status === 'IN_PROGRESS').length;
  }
  get completedLabOrders(): number {
    return this.labTests.filter(t => t.status === 'COMPLETED').length;
  }

  // Open Interactive Modal for Card Click
  openCardFeature(type: 'BEDS' | 'PHARMACY' | 'LAB' | 'QUEUE' | 'ANALYTICS' | 'TELEMED' | 'EMERGENCY' | 'SURGERY' | 'BLOOD_BANK' | 'INSURANCE' | 'ROSTER' | 'NOTIFICATIONS'): void {
    this.activeModal = type;
    if (type === 'EMERGENCY') {
      this.emergencyService.getActive().subscribe({ next: (res) => this.emergencyCases = res.data || [] });
    } else if (type === 'SURGERY') {
      this.surgeryService.getAll().subscribe({ next: (res) => this.surgeries = res.data || [] });
    } else if (type === 'BLOOD_BANK') {
      this.bloodBankService.getAll().subscribe({ next: (res) => this.bloodInventory = res.data || [] });
    } else if (type === 'INSURANCE') {
      this.insuranceService.getAll().subscribe({ next: (res) => this.claims = res.data || [] });
    } else if (type === 'ROSTER') {
      this.staffRosterService.getAll().subscribe({
        next: (res) => {
          this.shifts = res.data || [];
          this.onCallSpecialists = this.shifts.filter(s => s.onCall);
        }
      });
    } else if (type === 'NOTIFICATIONS') {
      this.notificationService.getRecent().subscribe({ next: (res) => this.notificationLogs = res.data || [] });
    } else if (type === 'BEDS') {
      this.bedService.getAllBeds().subscribe({ next: (res) => this.beds = res.data || [] });
    } else if (type === 'PHARMACY') {
      this.medicineService.getAllMedicines().subscribe({ next: (res) => this.medicines = res.data || [] });
    } else if (type === 'LAB') {
      this.labTestService.getAllLabTests().subscribe({ next: (res) => this.labTests = res.data || [] });
    }
  }

  closeModal(): void {
    this.stopSiren();
    this.activeModal = null;
  }

  navigateTo(path: string, queryParams?: any): void {
    this.closeModal();
    this.router.navigate([path], { queryParams });
  }

  // Quick Action: Quick Restock for low-stock medicine
  quickRestock(med: Medicine, amount: number): void {
    if (!med.id) return;
    this.medicineService.adjustStock(med.id, amount).subscribe({
      next: (res) => {
        med.stockQuantity = res.data?.stockQuantity ?? (med.stockQuantity + amount);
        this.successMessage = `Restocked ${amount} units of ${med.name}. New total: ${med.stockQuantity}`;
        setTimeout(() => this.successMessage = null, 3500);
      },
      error: (err) => {
        this.errorMessage = 'Restock failed: ' + (err.error?.message || err.message);
        setTimeout(() => this.errorMessage = null, 4000);
      }
    });
  }

  // Code Blue siren in operations hub
  triggerCodeBlueInHub(caseItem: EmergencyCase): void {
    if (!caseItem.id) return;
    this.playSiren();
    this.emergencyService.triggerCodeBlue(caseItem.id).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.emergencyCases.findIndex(c => c.id === caseItem.id);
          if (idx !== -1) this.emergencyCases[idx] = res.data;
          this.successMessage = `CODE BLUE alert broadcast for Case ${res.data.caseNumber}!`;
          setTimeout(() => this.successMessage = null, 4000);
        }
      }
    });
  }

  playSiren(): void {
    if (this.isSirenPlaying) return;
    try {
      const AudioCtxClass = window.AudioContext || (window as any).webkitAudioContext;
      if (!AudioCtxClass) return;
      if (!this.audioCtx) this.audioCtx = new AudioCtxClass();
      if (this.audioCtx.state === 'suspended') this.audioCtx.resume();

      this.sirenOsc = this.audioCtx.createOscillator();
      this.sirenGain = this.audioCtx.createGain();
      this.sirenOsc.type = 'sawtooth';

      const now = this.audioCtx.currentTime;
      this.sirenOsc.frequency.setValueAtTime(650, now);
      for (let i = 0; i < 15; i++) {
        this.sirenOsc.frequency.linearRampToValueAtTime(950, now + i * 0.6 + 0.3);
        this.sirenOsc.frequency.linearRampToValueAtTime(650, now + (i + 1) * 0.6);
      }
      this.sirenGain.gain.setValueAtTime(0.12, now);
      this.sirenOsc.connect(this.sirenGain);
      this.sirenGain.connect(this.audioCtx.destination);
      this.sirenOsc.start();
      this.isSirenPlaying = true;
    } catch (e) {
      console.warn('Siren audio note:', e);
    }
  }

  stopSiren(): void {
    if (this.sirenOsc) {
      try { this.sirenOsc.stop(); } catch(e) {}
      try { this.sirenOsc.disconnect(); } catch(e) {}
      this.sirenOsc = null;
    }
    this.isSirenPlaying = false;
  }

  // Quick action: Advance surgery status
  quickAdvanceSurgery(s: SurgerySchedule): void {
    if (!s.id) return;
    const nextStatus = s.status === 'SCHEDULED' ? 'IN_PROGRESS' : (s.status === 'IN_PROGRESS' ? 'IN_PACU' : 'COMPLETED');
    const pacuScore = nextStatus === 'IN_PACU' ? 8 : (nextStatus === 'COMPLETED' ? 10 : undefined);
    this.surgeryService.updateStatus(s.id, nextStatus, pacuScore).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.surgeries.findIndex(x => x.id === s.id);
          if (idx !== -1) this.surgeries[idx] = res.data;
          this.successMessage = `Procedure ${res.data.surgeryNumber} moved to ${res.data.status}`;
          setTimeout(() => this.successMessage = null, 3500);
        }
      }
    });
  }

  // Quick action: Reserve Blood Unit
  quickReserveBlood(item: BloodInventory): void {
    if (!item.id || item.unitsAvailable <= 0) return;
    this.bloodBankService.reserveUnits(item.id, 1).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.bloodInventory.findIndex(x => x.id === item.id);
          if (idx !== -1) this.bloodInventory[idx] = res.data;
          this.successMessage = `Reserved 1 unit of ${res.data.bloodGroup} (${res.data.componentType})`;
          setTimeout(() => this.successMessage = null, 3500);
        }
      }
    });
  }

  // Quick action: Restock Blood
  quickRestockBlood(item: BloodInventory): void {
    if (!item.id) return;
    this.bloodBankService.restockUnits(item.id, 5).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.bloodInventory.findIndex(x => x.id === item.id);
          if (idx !== -1) this.bloodInventory[idx] = res.data;
          this.successMessage = `Restocked +5 units of ${res.data.bloodGroup}!`;
          setTimeout(() => this.successMessage = null, 3500);
        }
      }
    });
  }

  // Quick action: Approve Claim Pre-Auth
  quickApproveClaim(c: InsuranceClaim): void {
    if (!c.id) return;
    this.insuranceService.updatePreAuth(c.id, 'PRE_AUTH_APPROVED', c.claimAmount, 'Pre-Auth guarantee letter issued').subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.claims.findIndex(x => x.id === c.id);
          if (idx !== -1) this.claims[idx] = res.data;
          this.successMessage = `Claim ${res.data.claimNumber} approved for ₹${res.data.approvedAmount}!`;
          setTimeout(() => this.successMessage = null, 3500);
        }
      }
    });
  }

  // Quick action: Toggle On-Call Staff
  quickToggleOnCall(shift: StaffShift): void {
    if (!shift.id) return;
    const nextVal = !shift.onCall;
    this.staffRosterService.toggleOnCall(shift.id, nextVal).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.shifts.findIndex(x => x.id === shift.id);
          if (idx !== -1) this.shifts[idx] = res.data;
          this.onCallSpecialists = this.shifts.filter(s => s.onCall);
          this.successMessage = `${res.data.staffName} is ${nextVal ? 'now ON-CALL' : 'released from on-call'}`;
          setTimeout(() => this.successMessage = null, 3500);
        }
      }
    });
  }

  // Quick action: Quick Dispatch Alert
  quickDispatchOutbox(): void {
    if (!this.quickAlertText) return;
    this.isDispatchingQuickAlert = true;
    const log: NotificationLog = {
      recipientName: 'Hospital Broadcast Network',
      recipientContact: '+1-555-0199',
      channel: this.quickAlertChannel,
      triggerEvent: 'OPERATIONS_HUB_BROADCAST',
      subject: 'Command Hub Alert',
      message: this.quickAlertText,
      status: 'DELIVERED'
    };
    this.notificationService.dispatchNotification(log).subscribe({
      next: (res) => {
        this.isDispatchingQuickAlert = false;
        if (res.success && res.data) {
          this.notificationLogs.unshift(res.data);
          this.successMessage = `Alert dispatched via ${res.data.channel}!`;
          setTimeout(() => this.successMessage = null, 3500);
        }
      },
      error: () => {
        this.isDispatchingQuickAlert = false;
      }
    });
  }

  // Sound chime synthesizer for OPD Queue Preview
  playQueueChime(): void {
    try {
      if (!this.audioCtx) {
        const AudioCtxClass = window.AudioContext || (window as any).webkitAudioContext;
        if (AudioCtxClass) {
          this.audioCtx = new AudioCtxClass();
        }
      }
      if (!this.audioCtx) return;

      if (this.audioCtx.state === 'suspended') {
        this.audioCtx.resume();
      }

      this.isChimePlaying = true;
      const now = this.audioCtx.currentTime;

      // Note 1 (High chime: 659.25 Hz - E5)
      const osc1 = this.audioCtx.createOscillator();
      const gain1 = this.audioCtx.createGain();
      osc1.type = 'sine';
      osc1.frequency.setValueAtTime(659.25, now);
      gain1.gain.setValueAtTime(0.3, now);
      gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.6);
      osc1.connect(gain1);
      gain1.connect(this.audioCtx.destination);
      osc1.start(now);
      osc1.stop(now + 0.6);

      // Note 2 (Low chime: 523.25 Hz - C5)
      const osc2 = this.audioCtx.createOscillator();
      const gain2 = this.audioCtx.createGain();
      osc2.type = 'sine';
      osc2.frequency.setValueAtTime(523.25, now + 0.25);
      gain2.gain.setValueAtTime(0.3, now + 0.25);
      gain2.gain.exponentialRampToValueAtTime(0.001, now + 0.9);
      osc2.connect(gain2);
      gain2.connect(this.audioCtx.destination);
      osc2.start(now + 0.25);
      osc2.stop(now + 0.9);

      setTimeout(() => this.isChimePlaying = false, 1000);
    } catch {
      this.isChimePlaying = false;
    }
  }
}
