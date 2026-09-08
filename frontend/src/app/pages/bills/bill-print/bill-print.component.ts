import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BillService } from '../../../core/services/bill.service';
import { Bill } from '../../../models/bill.model';

@Component({
  selector: 'app-bill-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './bill-print.component.html',
  styleUrls: ['./bill-print.component.css']
})
export class BillPrintComponent implements OnInit {
  billId!: number;
  bill: Bill | null = null;
  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private billService: BillService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.billId = Number(idParam);
      this.loadBill();
    } else {
      this.router.navigate(['/bills']);
    }
  }

  loadBill(): void {
    this.isLoading = true;
    this.billService.getById(this.billId).subscribe({
      next: (res) => {
        this.bill = res || null;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load bill invoice';
        this.isLoading = false;
      }
    });
  }

  printInvoice(): void {
    window.print();
  }
}
