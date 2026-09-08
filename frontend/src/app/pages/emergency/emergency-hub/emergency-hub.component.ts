import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EmergencyService } from '../../../core/services/emergency.service';
import { AuthService } from '../../../core/services/auth.service';
import { EmergencyCase } from '../../../models/emergency.model';

@Component({
  selector: 'app-emergency-hub',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './emergency-hub.component.html',
  styleUrls: ['./emergency-hub.component.css']
})
export class EmergencyHubComponent implements OnInit, OnDestroy {
  cases: EmergencyCase[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  selectedEsiFilter: string = 'ALL';
  selectedStatusFilter: string = 'ACTIVE';

  // Code Blue alert state
  activeCodeBlueCase: EmergencyCase | null = null;
  showCodeBlueModal = false;
  isAlarmPlaying = false;
  private audioCtx: any = null;
  private sirenOsc: any = null;
  private sirenGain: any = null;

  // New Case Modal
  showNewCaseModal = false;
  isSubmitting = false;
  hasAmbulance = false;
  newCase: EmergencyCase = {
    caseNumber: '',
    patientName: '',
    patientAge: 35,
    gender: 'MALE',
    chiefComplaint: '',
    triageLevel: 'EMERGENT',
    status: 'TRIAGED',
    heartRate: 98,
    bloodPressure: '130/85',
    spo2: 96,
    ambulanceNumber: '',
    etaMinutes: 0
  };

  // Quick Action Modal
  selectedCaseForAction: EmergencyCase | null = null;
  showActionModal = false;
  actionStatus: 'TRIAGED' | 'IN_TREATMENT' | 'ADMITTED' | 'DISCHARGED' = 'IN_TREATMENT';
  actionBedNumber: string = '';

  constructor(
    public emergencyService: EmergencyService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadCases();
  }

  ngOnDestroy(): void {
    this.stopSiren();
  }

  loadCases(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.emergencyService.getAll().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.cases = res.data;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load emergency cases.';
        this.isLoading = false;
      }
    });
  }

  get filteredCases(): EmergencyCase[] {
    return this.cases.filter(c => {
      const matchEsi = this.selectedEsiFilter === 'ALL' || c.triageLevel === this.selectedEsiFilter;
      let matchStatus = true;
      if (this.selectedStatusFilter === 'ACTIVE') {
        matchStatus = c.status === 'TRIAGED' || c.status === 'IN_TREATMENT';
      } else if (this.selectedStatusFilter !== 'ALL') {
        matchStatus = c.status === this.selectedStatusFilter;
      }
      return matchEsi && matchStatus;
    });
  }

  get esi1Count(): number {
    return this.cases.filter(c => c.triageLevel === 'RESUSCITATION' && c.status !== 'DISCHARGED').length;
  }

  get activeCasesCount(): number {
    return this.cases.filter(c => c.status === 'TRIAGED' || c.status === 'IN_TREATMENT').length;
  }

  get inboundAmbulancesCount(): number {
    return this.cases.filter(c => (c.etaMinutes ?? 0) > 0 && c.status !== 'DISCHARGED').length;
  }

  get codeBlueActive(): boolean {
    return this.cases.some(c => c.codeBlueTriggered && c.status !== 'DISCHARGED');
  }

  openNewCaseModal(): void {
    this.hasAmbulance = false;
    this.newCase = {
      caseNumber: 'ER-' + Math.floor(100000 + Math.random() * 900000),
      patientName: '',
      patientAge: 40,
      gender: 'MALE',
      chiefComplaint: '',
      triageLevel: 'EMERGENT',
      status: 'TRIAGED',
      heartRate: 92,
      bloodPressure: '125/82',
      spo2: 97,
      ambulanceNumber: '',
      etaMinutes: 0
    };
    this.showNewCaseModal = true;
  }

  closeNewCaseModal(): void {
    this.showNewCaseModal = false;
  }

  saveNewCase(): void {
    if (!this.newCase.patientName || !this.newCase.chiefComplaint) {
      alert('Please fill in patient name and chief complaint.');
      return;
    }
    if (!this.hasAmbulance) {
      this.newCase.ambulanceNumber = undefined;
      this.newCase.etaMinutes = 0;
    }
    this.isSubmitting = true;
    this.emergencyService.createCase(this.newCase).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        if (res.success && res.data) {
          this.cases.unshift(res.data);
          this.successMessage = `Emergency case ${res.data.caseNumber} triaged successfully!`;
          setTimeout(() => this.successMessage = null, 4000);
          this.closeNewCaseModal();
        }
      },
      error: (err) => {
        this.isSubmitting = false;
        alert('Failed to create emergency case: ' + (err.error?.message || err.message));
      }
    });
  }

  triggerCodeBlue(c: EmergencyCase): void {
    this.activeCodeBlueCase = c;
    this.showCodeBlueModal = true;
    this.playSiren();

    if (c.id) {
      this.emergencyService.triggerCodeBlue(c.id).subscribe({
        next: (res) => {
          if (res.success && res.data) {
            const idx = this.cases.findIndex(x => x.id === c.id);
            if (idx !== -1) this.cases[idx] = res.data;
          }
        }
      });
    }
  }

  dismissCodeBlue(): void {
    this.stopSiren();
    this.showCodeBlueModal = false;
    this.activeCodeBlueCase = null;
  }

  playSiren(): void {
    if (this.isAlarmPlaying) return;
    try {
      const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
      if (!AudioCtx) return;
      this.audioCtx = new AudioCtx();
      this.sirenOsc = this.audioCtx.createOscillator();
      this.sirenGain = this.audioCtx.createGain();
      this.sirenOsc.type = 'sawtooth';
      
      const now = this.audioCtx.currentTime;
      this.sirenOsc.frequency.setValueAtTime(650, now);
      for (let i = 0; i < 20; i++) {
        this.sirenOsc.frequency.linearRampToValueAtTime(950, now + i * 0.6 + 0.3);
        this.sirenOsc.frequency.linearRampToValueAtTime(650, now + (i + 1) * 0.6);
      }
      this.sirenGain.gain.setValueAtTime(0.12, now);
      this.sirenOsc.connect(this.sirenGain);
      this.sirenGain.connect(this.audioCtx.destination);
      this.sirenOsc.start();
      this.isAlarmPlaying = true;
    } catch (e) {
      console.warn('Audio siren note: ', e);
    }
  }

  stopSiren(): void {
    if (this.sirenOsc) {
      try { this.sirenOsc.stop(); } catch(e) {}
      try { this.sirenOsc.disconnect(); } catch(e) {}
      this.sirenOsc = null;
    }
    if (this.audioCtx) {
      try { this.audioCtx.close(); } catch(e) {}
      this.audioCtx = null;
    }
    this.isAlarmPlaying = false;
  }

  openActionModal(c: EmergencyCase): void {
    this.selectedCaseForAction = c;
    this.actionStatus = c.status;
    this.actionBedNumber = c.assignedBedNumber || '';
    this.showActionModal = true;
  }

  closeActionModal(): void {
    this.showActionModal = false;
    this.selectedCaseForAction = null;
  }

  updateCaseStatus(): void {
    if (!this.selectedCaseForAction || !this.selectedCaseForAction.id) return;
    this.emergencyService.updateStatus(this.selectedCaseForAction.id, this.actionStatus, this.actionBedNumber).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.cases.findIndex(x => x.id === this.selectedCaseForAction!.id);
          if (idx !== -1) this.cases[idx] = res.data;
          this.successMessage = `Case ${res.data.caseNumber} updated to ${res.data.status}`;
          setTimeout(() => this.successMessage = null, 3500);
        }
        this.closeActionModal();
      },
      error: (err) => {
        alert('Failed to update status: ' + (err.error?.message || err.message));
      }
    });
  }

  getEsiBadgeClass(level: string): string {
    switch (level) {
      case 'RESUSCITATION': return 'badge bg-danger text-white pulse-glow';
      case 'EMERGENT': return 'badge bg-warning text-dark';
      case 'URGENT': return 'badge bg-primary text-white';
      case 'LESS_URGENT': return 'badge bg-info text-dark';
      case 'NON_URGENT': return 'badge bg-secondary text-white';
      default: return 'badge bg-light text-dark';
    }
  }

  getEsiLabel(level: string): string {
    switch (level) {
      case 'RESUSCITATION': return 'ESI-1 Resuscitation (Immediate)';
      case 'EMERGENT': return 'ESI-2 Emergent (<15 mins)';
      case 'URGENT': return 'ESI-3 Urgent (<30 mins)';
      case 'LESS_URGENT': return 'ESI-4 Less Urgent (<60 mins)';
      case 'NON_URGENT': return 'ESI-5 Non-Urgent';
      default: return level;
    }
  }
}
