import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { BillService } from '../../../core/services/bill.service';
import { AuthService } from '../../../core/services/auth.service';
import { Bill, PaymentStatus } from '../../../models/bill.model';

@Component({
  selector: 'app-bill-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './bill-list.component.html',
  styleUrls: ['./bill-list.component.css']
})
export class BillListComponent implements OnInit {
  bills: Bill[] = [];
  filteredBills: Bill[] = [];
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  // Filter models
  searchTerm = '';
  filterDate = '';
  filterStatus = '';

  // Modal payment state
  selectedBillForPayment: Bill | null = null;
  selectedPaymentMethod = 'UPI';
  paymentNotes = '';
  isProcessingPayment = false;

  // UPI payment configuration & state
  hospitalUpiId = 'medpulse.hospital@icici';
  hospitalPayeeName = 'MedPulse Hospital';
  copiedUpi = false;
  copiedLink = false;
  utrNumber = '';
  qrImageError = false;
  activeUpiTab: 'qr' | 'apps' = 'qr';
  pendingPayBillId: number | null = null;

  // Multi-device UPI Apps & Collect Request state
  isMobile = false;
  selectedUpiApp = '';
  desktopAppPrompt = '';
  patientVpa = '';
  isCollectRequestSent = false;
  collectCountdown = 300;
  collectTimerInterval: any = null;

  constructor(
    private billService: BillService,
    public authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.detectDevice();
    this.route.queryParams.subscribe(params => {
      if (params['status'] || params['paymentStatus']) {
        this.filterStatus = (params['status'] || params['paymentStatus']).toUpperCase();
      }
      if (params['date']) {
        this.filterDate = params['date'];
      }
      if (params['filter'] === 'today') {
        const d = new Date();
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        this.filterDate = `${year}-${month}-${day}`;
      }
      if (params['search']) {
        this.searchTerm = params['search'];
      }
      if (params['payBillId']) {
        this.pendingPayBillId = Number(params['payBillId']);
      }
      this.loadBills();
    });
  }

  detectDevice(): void {
    if (typeof window !== 'undefined' && window.navigator) {
      this.isMobile = /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(window.navigator.userAgent);
    }
  }

  loadBills(): void {
    this.isLoading = true;
    this.errorMessage = '';

    const filters: any = {};
    if (this.searchTerm.trim()) filters.search = this.searchTerm.trim();
    if (this.filterDate) filters.date = this.filterDate;
    if (this.filterStatus) filters.paymentStatus = this.filterStatus;

    this.billService.getAll(filters).subscribe({
      next: (bills) => {
        this.bills = bills || [];
        this.filteredBills = [...this.bills];
        this.isLoading = false;

        // Auto-open payment modal if navigated with payBillId
        if (this.pendingPayBillId) {
          const target = this.bills.find(b => b.id === this.pendingPayBillId);
          if (target && (target.paymentStatus === 'PENDING' || target.paymentStatus === 'UNPAID')) {
            this.openPaymentModal(target);
          }
          this.pendingPayBillId = null;
        }
      },
      error: (err) => {
        this.errorMessage = 'Failed to load bills. ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  onFilterChange(): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        status: this.filterStatus ? this.filterStatus : null,
        date: this.filterDate ? this.filterDate : null,
        search: this.searchTerm ? this.searchTerm : null
      },
      queryParamsHandling: 'merge'
    });
    this.loadBills();
  }

  filterByStatus(status: string): void {
    this.filterStatus = status;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { status: status ? status : null },
      queryParamsHandling: 'merge'
    });
    this.loadBills();
  }

  get isAnyFilterActive(): boolean {
    return !!this.filterStatus || !!this.filterDate || !!this.searchTerm;
  }

  get activeFilterTitle(): string {
    const parts: string[] = [];
    if (this.filterStatus) parts.push(`Status: ${this.filterStatus}`);
    if (this.filterDate) parts.push(`Date: ${this.filterDate}`);
    if (this.searchTerm) parts.push(`Search: "${this.searchTerm}"`);
    return parts.join(' | ') || 'All Invoices';
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.filterDate = '';
    this.filterStatus = '';
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {}
    });
    this.loadBills();
  }

  openPaymentModal(bill: Bill): void {
    this.selectedBillForPayment = bill;
    this.selectedPaymentMethod = 'UPI';
    this.paymentNotes = '';
    this.copiedUpi = false;
    this.copiedLink = false;
    this.qrImageError = false;
    this.activeUpiTab = 'qr';
    this.selectedUpiApp = '';
    this.desktopAppPrompt = '';
    this.isCollectRequestSent = false;
    this.patientVpa = 'patient@okhdfcbank';
    if (this.collectTimerInterval) {
      clearInterval(this.collectTimerInterval);
      this.collectTimerInterval = null;
    }
    this.generateSimulatedUtr();
  }

  closePaymentModal(): void {
    this.selectedBillForPayment = null;
    this.copiedUpi = false;
    this.copiedLink = false;
    this.utrNumber = '';
    this.selectedUpiApp = '';
    this.desktopAppPrompt = '';
    this.isCollectRequestSent = false;
    if (this.collectTimerInterval) {
      clearInterval(this.collectTimerInterval);
      this.collectTimerInterval = null;
    }
  }

  getUpiPaymentUrl(): string {
    if (!this.selectedBillForPayment) return '';
    const pa = this.hospitalUpiId;
    const pn = encodeURIComponent(this.hospitalPayeeName);
    const mc = '8062';
    const tr = encodeURIComponent(this.selectedBillForPayment.billNumber);
    const tn = encodeURIComponent(`Bill ${this.selectedBillForPayment.billNumber} Settlement`);
    const am = encodeURIComponent(Number(this.selectedBillForPayment.totalAmount).toFixed(2));
    const cu = 'INR';
    return `upi://pay?pa=${pa}&pn=${pn}&mc=${mc}&tr=${tr}&tn=${tn}&am=${am}&cu=${cu}`;
  }

  getAppIntentUrl(packageName: string): string {
    if (!this.selectedBillForPayment) return '';
    const pa = this.hospitalUpiId;
    const pn = encodeURIComponent(this.hospitalPayeeName);
    const mc = '8062';
    const tr = encodeURIComponent(this.selectedBillForPayment.billNumber);
    const tn = encodeURIComponent(`Bill ${this.selectedBillForPayment.billNumber} Settlement`);
    const am = encodeURIComponent(Number(this.selectedBillForPayment.totalAmount).toFixed(2));
    const cu = 'INR';

    if (packageName === 'upi') {
      return `upi://pay?pa=${pa}&pn=${pn}&mc=${mc}&tr=${tr}&tn=${tn}&am=${am}&cu=${cu}`;
    }
    return `intent://pay?pa=${pa}&pn=${pn}&mc=${mc}&tr=${tr}&tn=${tn}&am=${am}&cu=${cu}#Intent;scheme=upi;package=${packageName};end;`;
  }

  getQrCodeUrl(): string {
    const upiUri = this.getUpiPaymentUrl();
    return `https://api.qrserver.com/v1/create-qr-code/?size=260x260&margin=10&data=${encodeURIComponent(upiUri)}`;
  }

  onQrError(): void {
    this.qrImageError = true;
  }

  copyUpiId(): void {
    if (navigator?.clipboard) {
      navigator.clipboard.writeText(this.hospitalUpiId).then(() => {
        this.copiedUpi = true;
        setTimeout(() => this.copiedUpi = false, 2500);
      });
    } else {
      this.copiedUpi = true;
      setTimeout(() => this.copiedUpi = false, 2500);
    }
  }

  copyUpiLink(): void {
    const link = this.getUpiPaymentUrl();
    if (navigator?.clipboard && link) {
      navigator.clipboard.writeText(link).then(() => {
        this.copiedLink = true;
        setTimeout(() => this.copiedLink = false, 2500);
      });
    } else {
      this.copiedLink = true;
      setTimeout(() => this.copiedLink = false, 2500);
    }
  }

  generateSimulatedUtr(): void {
    const now = new Date();
    const year = now.getFullYear().toString().slice(-2);
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const randomDigits = Math.floor(100000 + Math.random() * 900000);
    this.utrNumber = `${year}${month}${day}${randomDigits}`;
  }

  onAppClick(appName: string, packageName: string, event?: Event): void {
    this.selectedUpiApp = appName;
    const intentUrl = this.getAppIntentUrl(packageName);

    if (this.isMobile) {
      // On mobile devices, allow direct intent or universal UPI launch
      if (event) {
        // Native anchor handles it, but fallback if needed
        setTimeout(() => {
          window.location.href = intentUrl;
        }, 100);
      } else {
        window.location.href = intentUrl;
      }
    } else {
      // On Desktop/Laptop: Native UPI app is not installed.
      if (event) event.preventDefault();
      const defaultSuffix = appName === 'Google Pay' ? '@okaxis' : appName === 'PhonePe' ? '@ybl' : appName === 'Paytm' ? '@paytm' : '@upi';
      const baseName = this.patientVpa.split('@')[0] || 'patient';
      this.patientVpa = baseName + defaultSuffix;
      this.desktopAppPrompt = `You are on a desktop browser. Enter your UPI ID below to receive a payment request on your ${appName} mobile app, or scan the QR code.`;
    }
  }

  sendCollectRequest(): void {
    if (!this.patientVpa || !this.patientVpa.includes('@')) {
      this.patientVpa = 'patient@okhdfcbank';
    }
    this.isCollectRequestSent = true;
    this.generateSimulatedUtr();
    this.collectCountdown = 300;
    if (this.collectTimerInterval) clearInterval(this.collectTimerInterval);
    this.collectTimerInterval = setInterval(() => {
      if (this.collectCountdown > 0) {
        this.collectCountdown--;
      } else {
        clearInterval(this.collectTimerInterval);
      }
    }, 1000);
  }

  cancelCollectRequest(): void {
    this.isCollectRequestSent = false;
    if (this.collectTimerInterval) {
      clearInterval(this.collectTimerInterval);
      this.collectTimerInterval = null;
    }
  }

  approveCollectPayment(): void {
    if (!this.utrNumber) {
      this.generateSimulatedUtr();
    }
    this.confirmPayment();
  }

  appendVpaSuffix(suffix: string): void {
    const base = this.patientVpa.split('@')[0] || 'patient';
    this.patientVpa = base + suffix;
  }

  get formattedCountdown(): string {
    const mins = Math.floor(this.collectCountdown / 60);
    const secs = this.collectCountdown % 60;
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
  }

  confirmPayment(): void {
    if (!this.selectedBillForPayment) return;

    this.isProcessingPayment = true;

    let finalNotes = (this.paymentNotes || '').trim();
    if (this.selectedPaymentMethod === 'UPI') {
      const utr = this.utrNumber.trim() || 'UPI-REF-' + Date.now().toString().slice(-8);
      const appInfo = this.selectedUpiApp ? ` via ${this.selectedUpiApp}` : '';
      const vpaInfo = this.isCollectRequestSent ? ` (Collect: ${this.patientVpa})` : '';
      finalNotes = finalNotes ? `UPI${appInfo}${vpaInfo} Ref/UTR: ${utr} | ${finalNotes}` : `UPI${appInfo}${vpaInfo} Ref/UTR: ${utr}`;
    }

    this.billService.markPaid(this.selectedBillForPayment.id, {
      paymentMethod: this.selectedPaymentMethod,
      paymentStatus: 'PAID',
      notes: finalNotes
    }).subscribe({
      next: (updatedBill) => {
        this.successMessage = `Payment of $${Number(updatedBill.totalAmount).toFixed(2)} confirmed for ${updatedBill.billNumber} via ${updatedBill.paymentMethod}!`;
        this.isProcessingPayment = false;
        this.closePaymentModal();
        this.loadBills();
        setTimeout(() => this.successMessage = '', 4000);
      },
      error: (err) => {
        this.errorMessage = 'Payment recording failed: ' + (err.error?.message || err.message);
        this.isProcessingPayment = false;
      }
    });
  }

  get totalPendingCount(): number {
    return this.bills.filter(b => b.paymentStatus === 'PENDING' || b.paymentStatus === 'UNPAID').length;
  }

  get totalPaidCount(): number {
    return this.bills.filter(b => b.paymentStatus === 'PAID').length;
  }

  get totalCollectedRevenue(): number {
    return this.bills
      .filter(b => b.paymentStatus === 'PAID')
      .reduce((sum, b) => sum + (Number(b.totalAmount) || 0), 0);
  }
}
