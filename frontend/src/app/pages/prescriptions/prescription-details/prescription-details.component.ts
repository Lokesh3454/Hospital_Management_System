import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PrescriptionService } from '../../../core/services/prescription.service';
import { AuthService } from '../../../core/services/auth.service';
import { Prescription } from '../../../models/prescription.model';

@Component({
  selector: 'app-prescription-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './prescription-details.component.html',
  styleUrls: ['./prescription-details.component.css']
})
export class PrescriptionDetailsComponent implements OnInit {
  prescriptionId!: number;
  prescription: Prescription | null = null;
  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private prescriptionService: PrescriptionService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.prescriptionId = Number(idParam);
      this.loadPrescription();
    } else {
      this.router.navigate(['/prescriptions']);
    }
  }

  get canEdit(): boolean {
    return this.authService.hasAnyRole(['ROLE_DOCTOR', 'ROLE_ADMIN']);
  }

  loadPrescription(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.prescriptionService.getById(this.prescriptionId).subscribe({
      next: (res) => {
        this.prescription = res.data || null;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load prescription.';
        this.isLoading = false;
      }
    });
  }

  printPrescription(): void {
    window.print();
  }
}
