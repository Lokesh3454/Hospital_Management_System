import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BillService } from '../../../core/services/bill.service';
import { AuthService } from '../../../core/services/auth.service';
import { Bill } from '../../../models/bill.model';

@Component({
  selector: 'app-bill-details',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './bill-details.component.html',
  styleUrls: ['./bill-details.component.css']
})
export class BillDetailsComponent implements OnInit {
  bill: Bill | null = null;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  // Payment modal state
  showPaymentModal = false;
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

  // Multi-device UPI Apps & Collect Request state
  isMobile = false;
  selectedUpiApp = '';
  desktopAppPrompt = '';
  patientVpa = '';
  isCollectRequestSent = false;
  collectCountdown = 300;
  collectTimerInterval: any = null;

  constructor(
    private route: ActivatedRoute,
    private billService: BillService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.detectDevice();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadBill(Number(id));
    }
  }

  detectDevice(): void {
    if (typeof window !== 'undefined' && window.navigator) {
      this.isMobile = /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(window.navigator.userAgent);
    }
  }

  loadBill(id: number): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.billService.getById(id).subscribe({
      next: (bill) => {
        this.bill = bill;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load bill details: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  printInvoice(): void {
    window.print();
  }

  openPaymentModal(): void {
    this.showPaymentModal = true;
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
    this.showPaymentModal = false;
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
    if (!this.bill) return '';
    const pa = this.hospitalUpiId;
    const pn = encodeURIComponent(this.hospitalPayeeName);
    const mc = '8062';
    const tr = encodeURIComponent(this.bill.billNumber);
    const tn = encodeURIComponent(`Bill ${this.bill.billNumber} Settlement`);
    const am = encodeURIComponent(Number(this.bill.totalAmount).toFixed(2));
    const cu = 'INR';
    return `upi://pay?pa=${pa}&pn=${pn}&mc=${mc}&tr=${tr}&tn=${tn}&am=${am}&cu=${cu}`;
  }

  getAppIntentUrl(packageName: string): string {
    if (!this.bill) return '';
    const pa = this.hospitalUpiId;
    const pn = encodeURIComponent(this.hospitalPayeeName);
    const mc = '8062';
    const tr = encodeURIComponent(this.bill.billNumber);
    const tn = encodeURIComponent(`Bill ${this.bill.billNumber} Settlement`);
    const am = encodeURIComponent(Number(this.bill.totalAmount).toFixed(2));
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
      if (event) {
        setTimeout(() => {
          window.location.href = intentUrl;
        }, 100);
      } else {
        window.location.href = intentUrl;
      }
    } else {
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
    if (!this.bill) return;

    this.isProcessingPayment = true;

    let finalNotes = (this.paymentNotes || '').trim();
    if (this.selectedPaymentMethod === 'UPI') {
      const utr = this.utrNumber.trim() || 'UPI-REF-' + Date.now().toString().slice(-8);
      const appInfo = this.selectedUpiApp ? ` via ${this.selectedUpiApp}` : '';
      const vpaInfo = this.isCollectRequestSent ? ` (Collect: ${this.patientVpa})` : '';
      finalNotes = finalNotes ? `UPI${appInfo}${vpaInfo} Ref/UTR: ${utr} | ${finalNotes}` : `UPI${appInfo}${vpaInfo} Ref/UTR: ${utr}`;
    }

    this.billService.markPaid(this.bill.id, {
      paymentMethod: this.selectedPaymentMethod,
      paymentStatus: 'PAID',
      notes: finalNotes
    }).subscribe({
      next: (updatedBill) => {
        this.bill = updatedBill;
        this.successMessage = `Payment of $${Number(updatedBill.totalAmount).toFixed(2)} successfully recorded via ${updatedBill.paymentMethod}!`;
        this.isProcessingPayment = false;
        this.closePaymentModal();
        setTimeout(() => this.successMessage = '', 4000);
      },
      error: (err) => {
        this.errorMessage = 'Payment recording failed: ' + (err.error?.message || err.message);
        this.isProcessingPayment = false;
      }
    });
  }
}
