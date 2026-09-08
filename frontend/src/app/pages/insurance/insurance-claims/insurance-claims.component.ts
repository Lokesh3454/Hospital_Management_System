import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { InsuranceService } from '../../../core/services/insurance.service';
import { AuthService } from '../../../core/services/auth.service';
import { InsuranceClaim } from '../../../models/insurance.model';

@Component({
  selector: 'app-insurance-claims',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './insurance-claims.component.html',
  styleUrls: ['./insurance-claims.component.css']
})
export class InsuranceClaimsComponent implements OnInit {
  claims: InsuranceClaim[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  selectedStatusFilter: string = 'ALL';

  // New Claim Modal
  showNewClaimModal = false;
  isSubmitting = false;
  newClaim: InsuranceClaim = {
    claimNumber: '',
    patientName: '',
    policyNumber: '',
    insuranceProvider: 'Star Health Insurance',
    tpaName: 'Medi Assist TPA',
    totalBillAmount: 50000,
    claimAmount: 45000,
    patientCoPay: 5000,
    status: 'SUBMITTED',
    icdCode: 'I21.9 - Acute Myocardial Infarction',
    claimNotes: 'Cashless emergency admission pre-authorization request'
  };

  // Pre-Auth Adjudication Modal
  selectedClaimForAction: InsuranceClaim | null = null;
  showAdjudicateModal = false;
  adjudicateStatus: 'SUBMITTED' | 'PRE_AUTH_APPROVED' | 'UNDER_REVIEW' | 'SETTLED' | 'REJECTED' = 'PRE_AUTH_APPROVED';
  adjudicateApprovedAmount: number = 0;
  adjudicateNotes: string = '';

  constructor(
    public insuranceService: InsuranceService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadClaims();
  }

  loadClaims(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.insuranceService.getAll().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.claims = res.data;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load insurance claims.';
        this.isLoading = false;
      }
    });
  }

  get filteredClaims(): InsuranceClaim[] {
    return this.claims.filter(c => {
      return this.selectedStatusFilter === 'ALL' || c.status === this.selectedStatusFilter;
    });
  }

  get totalSubmittedAmount(): number {
    return this.claims.reduce((sum, c) => sum + (c.claimAmount || 0), 0);
  }

  get totalApprovedAmount(): number {
    return this.claims.reduce((sum, c) => sum + (c.approvedAmount || 0), 0);
  }

  get pendingReviewCount(): number {
    return this.claims.filter(c => c.status === 'SUBMITTED' || c.status === 'UNDER_REVIEW').length;
  }

  get settledCount(): number {
    return this.claims.filter(c => c.status === 'SETTLED').length;
  }

  openNewClaimModal(): void {
    const randomNum = Math.floor(100000 + Math.random() * 900000);
    this.newClaim = {
      claimNumber: `CLM-${randomNum}`,
      patientName: '',
      policyNumber: `POL-${Math.floor(10000000 + Math.random() * 90000000)}`,
      insuranceProvider: 'Star Health Insurance',
      tpaName: 'Medi Assist TPA',
      totalBillAmount: 60000,
      claimAmount: 54000,
      patientCoPay: 6000,
      status: 'SUBMITTED',
      icdCode: 'I21.9 - Acute Myocardial Infarction',
      claimNotes: 'Pre-authorization request for planned/emergency care'
    };
    this.showNewClaimModal = true;
  }

  closeNewClaimModal(): void {
    this.showNewClaimModal = false;
  }

  onBillAmountChange(): void {
    const bill = this.newClaim.totalBillAmount || 0;
    this.newClaim.patientCoPay = Math.round(bill * 0.10); // 10% co-pay
    this.newClaim.claimAmount = bill - this.newClaim.patientCoPay;
  }

  saveClaim(): void {
    if (!this.newClaim.patientName || !this.newClaim.policyNumber) {
      alert('Please enter patient name and policy number.');
      return;
    }
    this.isSubmitting = true;
    this.insuranceService.submitClaim(this.newClaim).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        if (res.success && res.data) {
          this.claims.unshift(res.data);
          this.successMessage = `Pre-auth claim ${res.data.claimNumber} filed successfully!`;
          setTimeout(() => this.successMessage = null, 4000);
          this.closeNewClaimModal();
        }
      },
      error: (err) => {
        this.isSubmitting = false;
        alert('Failed to submit claim: ' + (err.error?.message || err.message));
      }
    });
  }

  openAdjudicateModal(claim: InsuranceClaim): void {
    this.selectedClaimForAction = claim;
    this.adjudicateStatus = claim.status === 'SUBMITTED' ? 'PRE_AUTH_APPROVED' : claim.status;
    this.adjudicateApprovedAmount = claim.approvedAmount || claim.claimAmount;
    this.adjudicateNotes = claim.claimNotes || '';
    this.showAdjudicateModal = true;
  }

  closeAdjudicateModal(): void {
    this.showAdjudicateModal = false;
    this.selectedClaimForAction = null;
  }

  saveAdjudication(): void {
    if (!this.selectedClaimForAction || !this.selectedClaimForAction.id) return;
    this.insuranceService.updatePreAuth(
      this.selectedClaimForAction.id,
      this.adjudicateStatus,
      this.adjudicateApprovedAmount,
      this.adjudicateNotes
    ).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.claims.findIndex(x => x.id === this.selectedClaimForAction!.id);
          if (idx !== -1) this.claims[idx] = res.data;
          this.successMessage = `Claim ${res.data.claimNumber} status updated to ${res.data.status}!`;
          setTimeout(() => this.successMessage = null, 3500);
        }
        this.closeAdjudicateModal();
      },
      error: (err) => {
        alert('Failed to update pre-auth: ' + (err.error?.message || err.message));
      }
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PRE_AUTH_APPROVED': return 'badge bg-success';
      case 'SUBMITTED': return 'badge bg-primary';
      case 'UNDER_REVIEW': return 'badge bg-warning text-dark';
      case 'SETTLED': return 'badge bg-info text-dark';
      case 'REJECTED': return 'badge bg-danger';
      default: return 'badge bg-secondary';
    }
  }
}
