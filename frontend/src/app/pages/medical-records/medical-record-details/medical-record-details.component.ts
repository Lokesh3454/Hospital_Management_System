import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MedicalRecordService } from '../../../core/services/medical-record.service';
import { AuthService } from '../../../core/services/auth.service';
import { MedicalRecord } from '../../../models/medical-record.model';

@Component({
  selector: 'app-medical-record-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './medical-record-details.component.html',
  styleUrls: ['./medical-record-details.component.css']
})
export class MedicalRecordDetailsComponent implements OnInit {
  recordId!: number;
  record: MedicalRecord | null = null;
  patientHistory: MedicalRecord[] = [];
  isLoading = false;
  isLoadingHistory = false;
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private medicalRecordService: MedicalRecordService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.recordId = Number(idParam);
      this.loadRecord();
    } else {
      this.router.navigate(['/medical-records']);
    }
  }

  get canEdit(): boolean {
    return this.authService.hasAnyRole(['ROLE_DOCTOR', 'ROLE_ADMIN']);
  }

  loadRecord(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.medicalRecordService.getById(this.recordId).subscribe({
      next: (res) => {
        this.record = res.data || null;
        this.isLoading = false;
        if (this.record?.patientId) {
          this.loadPatientHistory(this.record.patientId);
        }
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load medical record.';
        this.isLoading = false;
      }
    });
  }

  loadPatientHistory(patientId: number): void {
    this.isLoadingHistory = true;
    this.medicalRecordService.getPatientHistory(patientId).subscribe({
      next: (res) => {
        this.patientHistory = res.data || [];
        this.isLoadingHistory = false;
      },
      error: () => {
        this.isLoadingHistory = false;
      }
    });
  }

  printRecord(): void {
    window.print();
  }
}
