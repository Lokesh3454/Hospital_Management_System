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

  // Payment modal
  showPaymentModal = false;
  selectedPaymentMethod = 'UPI';
  paymentNotes = '';
  isProcessingPayment = false;

  constructor(
    private route: ActivatedRoute,
    private billService: BillService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadBill(Number(id));
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
  }

  closePaymentModal(): void {
    this.showPaymentModal = false;
  }

  confirmPayment(): void {
    if (!this.bill) return;

    this.isProcessingPayment = true;
    this.billService.markPaid(this.bill.id, {
      paymentMethod: this.selectedPaymentMethod,
      paymentStatus: 'PAID',
      notes: this.paymentNotes
    }).subscribe({
      next: (updatedBill) => {
        this.bill = updatedBill;
        this.successMessage = `Payment of $${updatedBill.totalAmount} successfully recorded via ${updatedBill.paymentMethod}!`;
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
