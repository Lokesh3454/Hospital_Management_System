import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LabTestService } from '../../../core/services/lab-test.service';
import { PatientService } from '../../../core/services/patient.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { AuthService } from '../../../core/services/auth.service';
import { LabTest, LabTestStatus } from '../../../models/lab-test.model';
import { Patient } from '../../../models/patient.model';
import { Doctor } from '../../../models/doctor.model';

@Component({
  selector: 'app-lab-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './lab-management.component.html',
  styleUrls: ['./lab-management.component.css']
})
export class LabManagementComponent implements OnInit {
  labTests: LabTest[] = [];
  patients: Patient[] = [];
  doctors: Doctor[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  selectedStatus = 'ALL';
  selectedCategory = 'ALL';

  // Test Details Modal (Card / Row Click Feature)
  showTestDetailsModal = false;
  selectedTestForDetails: LabTest | null = null;

  // Publish Results Modal State
  showResultModal = false;
  selectedTestForResult: LabTest | null = null;
  resultValue = '';
  resultInterpretation = 'Normal';
  resultRemarks = '';
  isSubmittingResult = false;

  // New Lab Order Modal State
  showOrderModal = false;
  newOrder: LabTest = {
    testCode: 'LAB-CBC-' + Math.floor(100 + Math.random() * 900),
    testName: 'Complete Blood Count (CBC)',
    category: 'Hematology',
    patientId: 0,
    status: 'ORDERED',
    normalRange: '4.5 - 11.0 x10^3/uL',
    unit: 'x10^3/uL',
    cost: 450.0
  };
  isSubmittingOrder = false;

  constructor(
    private labTestService: LabTestService,
    private patientService: PatientService,
    private doctorService: DoctorService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadLabTests();
    this.loadPatients();
    this.loadDoctors();
  }

  loadLabTests(): void {
    this.isLoading = true;
    this.labTestService.getAllLabTests().subscribe({
      next: (res) => {
        this.labTests = res.data || [];
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load lab tests: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  loadPatients(): void {
    this.patientService.getAll().subscribe({
      next: (res) => this.patients = res.data || [],
      error: () => {}
    });
  }

  loadDoctors(): void {
    this.doctorService.getAll().subscribe({
      next: (res) => this.doctors = res.data || [],
      error: () => {}
    });
  }

  // Filter KPI Click
  filterByStatus(status: string): void {
    if (this.selectedStatus === status && status !== 'ALL') {
      this.selectedStatus = 'ALL';
    } else {
      this.selectedStatus = status;
    }
  }

  get filteredTests(): LabTest[] {
    return this.labTests.filter(t => {
      let matchStatus = true;
      if (this.selectedStatus === 'ALL') {
        matchStatus = true;
      } else if (this.selectedStatus === 'PENDING') {
        matchStatus = t.status !== 'COMPLETED' && t.status !== 'CANCELLED';
      } else {
        matchStatus = t.status === this.selectedStatus;
      }
      const matchCategory = this.selectedCategory === 'ALL' || t.category === this.selectedCategory;
      return matchStatus && matchCategory;
    });
  }

  get totalOrders(): number { return this.labTests.length; }
  get pendingOrders(): number {
    return this.labTests.filter(t => t.status !== 'COMPLETED' && t.status !== 'CANCELLED').length;
  }
  get completedOrders(): number {
    return this.labTests.filter(t => t.status === 'COMPLETED').length;
  }

  // ==========================================
  // CARD / ROW CLICK FEATURE: Lab Test Details Modal
  // ==========================================
  openTestDetails(test: LabTest): void {
    this.selectedTestForDetails = test;
    this.showTestDetailsModal = true;
  }

  closeTestDetailsModal(): void {
    this.showTestDetailsModal = false;
    this.selectedTestForDetails = null;
  }

  printLabReport(): void {
    window.print();
  }

  updateStatus(test: LabTest, newStatus: string): void {
    if (!test.id) return;
    this.labTestService.updateStatus(test.id, newStatus).subscribe({
      next: () => {
        test.status = newStatus as LabTestStatus;
        if (this.selectedTestForDetails && this.selectedTestForDetails.id === test.id) {
          this.selectedTestForDetails.status = newStatus as LabTestStatus;
        }
        this.successMessage = `Diagnostic order status updated to ${newStatus}`;
        this.loadLabTests();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.errorMessage = 'Failed to update status: ' + (err.error?.message || err.message);
        setTimeout(() => this.errorMessage = null, 4000);
      }
    });
  }

  openResultModal(test: LabTest): void {
    this.selectedTestForResult = test;
    this.resultValue = test.resultValue || '';
    this.resultInterpretation = test.interpretation || 'Normal';
    this.resultRemarks = test.remarks || '';
    this.showResultModal = true;
  }

  closeResultModal(): void {
    this.showResultModal = false;
    this.selectedTestForResult = null;
  }

  submitResult(): void {
    if (!this.selectedTestForResult || !this.selectedTestForResult.id) return;

    this.isSubmittingResult = true;
    this.labTestService.recordResults(
      this.selectedTestForResult.id,
      this.resultValue,
      this.resultInterpretation,
      this.resultRemarks
    ).subscribe({
      next: (res) => {
        this.isSubmittingResult = false;
        this.closeResultModal();
        if (this.selectedTestForDetails && this.selectedTestForDetails.id === res.data?.id) {
          this.selectedTestForDetails = res.data || null;
        }
        this.successMessage = `Clinical findings saved and report published for order #${this.selectedTestForResult?.testCode}.`;
        this.loadLabTests();
        setTimeout(() => this.successMessage = null, 4000);
      },
      error: (err) => {
        this.errorMessage = 'Failed to publish results: ' + (err.error?.message || err.message);
        this.isSubmittingResult = false;
      }
    });
  }

  saveResults(): void {
    this.submitResult();
  }

  openOrderModal(): void {
    this.newOrder = {
      testCode: 'LAB-' + Math.floor(1000 + Math.random() * 9000),
      testName: 'Complete Blood Count (CBC)',
      category: 'Hematology',
      patientId: this.patients.length > 0 ? (this.patients[0].patientId || this.patients[0].id || 0) : 0,
      doctorId: this.doctors.length > 0 ? this.doctors[0].doctorId : undefined,
      status: 'ORDERED',
      normalRange: '4.5 - 11.0 x10^3/uL',
      unit: 'x10^3/uL',
      cost: 450.0
    };
    this.showOrderModal = true;
  }

  closeOrderModal(): void {
    this.showOrderModal = false;
  }

  submitOrder(): void {
    if (!this.newOrder.patientId || !this.newOrder.testName) return;

    this.isSubmittingOrder = true;
    this.labTestService.createLabTest(this.newOrder).subscribe({
      next: () => {
        this.isSubmittingOrder = false;
        this.closeOrderModal();
        this.successMessage = `Diagnostic order for ${this.newOrder.testName} created successfully.`;
        this.loadLabTests();
        setTimeout(() => this.successMessage = null, 4000);
      },
      error: (err) => {
        this.errorMessage = 'Failed to create order: ' + (err.error?.message || err.message);
        this.isSubmittingOrder = false;
      }
    });
  }

  saveOrder(): void {
    this.submitOrder();
  }
}
