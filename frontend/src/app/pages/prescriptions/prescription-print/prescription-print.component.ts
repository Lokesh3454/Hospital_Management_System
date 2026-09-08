import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PrescriptionService } from '../../../core/services/prescription.service';
import { Prescription } from '../../../models/prescription.model';
import { PatientService } from '../../../core/services/patient.service';
import { Patient } from '../../../models/patient.model';

@Component({
  selector: 'app-prescription-print',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './prescription-print.component.html',
  styleUrls: ['./prescription-print.component.css']
})
export class PrescriptionPrintComponent implements OnInit {
  prescriptionId!: number;
  prescription: Prescription | null = null;
  patient: Patient | null = null;
  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private prescriptionService: PrescriptionService,
    private patientService: PatientService
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

  loadPrescription(): void {
    this.isLoading = true;
    this.prescriptionService.getById(this.prescriptionId).subscribe({
      next: (res) => {
        this.prescription = res.data || null;
        this.isLoading = false;
        if (this.prescription && this.prescription.patientId) {
          this.loadPatient(this.prescription.patientId);
        }
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load prescription';
        this.isLoading = false;
      }
    });
  }

  loadPatient(patientId: number): void {
    this.patientService.getById(patientId).subscribe({
      next: (res) => {
        this.patient = res.data || null;
      },
      error: () => {}
    });
  }

  printPrescription(): void {
    window.print();
  }
}
