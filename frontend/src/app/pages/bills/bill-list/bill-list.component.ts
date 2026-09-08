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

  constructor(
    private billService: BillService,
    public authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
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
      this.loadBills();
    });
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
  }

  closePaymentModal(): void {
    this.selectedBillForPayment = null;
  }

  confirmPayment(): void {
    if (!this.selectedBillForPayment) return;

    this.isProcessingPayment = true;
    this.billService.markPaid(this.selectedBillForPayment.id, {
      paymentMethod: this.selectedPaymentMethod,
      paymentStatus: 'PAID',
      notes: this.paymentNotes
    }).subscribe({
      next: (updatedBill) => {
        this.successMessage = `Payment confirmed for ${updatedBill.billNumber} via ${updatedBill.paymentMethod}!`;
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
