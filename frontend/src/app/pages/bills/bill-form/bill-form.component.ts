import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BillService } from '../../../core/services/bill.service';
import { PatientService } from '../../../core/services/patient.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { Patient } from '../../../models/patient.model';
import { Appointment } from '../../../models/appointment.model';
import { BillRequest } from '../../../models/bill.model';

@Component({
  selector: 'app-bill-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './bill-form.component.html',
  styleUrls: ['./bill-form.component.css']
})
export class BillFormComponent implements OnInit {
  billForm!: FormGroup;
  isEditMode = false;
  billId: number | null = null;
  isLoading = false;
  isSubmitting = false;
  errorMessage = '';

  patients: Patient[] = [];
  appointments: Appointment[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private billService: BillService,
    private patientService: PatientService,
    private appointmentService: AppointmentService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadDropdownData();
    this.checkRouteParams();
  }

  initForm(): void {
    const todayStr = new Date().toISOString().split('T')[0];
    this.billForm = this.fb.group({
      patientId: ['', [Validators.required]],
      appointmentId: [null],
      consultationFee: [0, [Validators.required, Validators.min(0)]],
      medicineCharges: [0, [Validators.required, Validators.min(0)]],
      testCharges: [0, [Validators.required, Validators.min(0)]],
      otherCharges: [0, [Validators.required, Validators.min(0)]],
      paymentStatus: ['PENDING', [Validators.required]],
      paymentMethod: [''],
      billDate: [todayStr, [Validators.required]],
      notes: ['']
    });
  }

  get computedTotal(): number {
    const cf = Number(this.billForm?.get('consultationFee')?.value) || 0;
    const mc = Number(this.billForm?.get('medicineCharges')?.value) || 0;
    const tc = Number(this.billForm?.get('testCharges')?.value) || 0;
    const oc = Number(this.billForm?.get('otherCharges')?.value) || 0;
    return cf + mc + tc + oc;
  }

  loadDropdownData(): void {
    this.patientService.getAll().subscribe({
      next: (res) => {
        this.patients = res.data || [];
      },
      error: () => {}
    });

    this.appointmentService.getAll().subscribe({
      next: (res) => {
        this.appointments = res.data || [];
      },
      error: () => {}
    });
  }

  checkRouteParams(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam && idParam !== 'new') {
      this.isEditMode = true;
      this.billId = Number(idParam);
      this.loadBillForEdit(this.billId);
      return;
    }

    // Check query params for pre-population
    this.route.queryParams.subscribe(params => {
      if (params['patientId']) {
        this.billForm.patchValue({ patientId: Number(params['patientId']) });
      }
      if (params['appointmentId']) {
        const apptId = Number(params['appointmentId']);
        this.billForm.patchValue({ appointmentId: apptId });
        this.fetchAppointmentDetails(apptId);
      }
    });
  }

  fetchAppointmentDetails(apptId: number): void {
    this.appointmentService.getById(apptId).subscribe({
      next: (res) => {
        if (res.data) {
          this.billForm.patchValue({
            patientId: res.data.patientId,
            consultationFee: 500.00
          });
        }
      }
    });
  }

  loadBillForEdit(id: number): void {
    this.isLoading = true;
    this.billService.getById(id).subscribe({
      next: (bill) => {
        this.billForm.patchValue({
          patientId: bill.patientId,
          appointmentId: bill.appointmentId || null,
          consultationFee: bill.consultationFee,
          medicineCharges: bill.medicineCharges,
          testCharges: bill.testCharges,
          otherCharges: bill.otherCharges,
          paymentStatus: bill.paymentStatus,
          paymentMethod: bill.paymentMethod || '',
          billDate: bill.billingDate ? bill.billingDate.split('T')[0] : '',
          notes: bill.notes || ''
        });
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load bill: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.billForm.invalid) {
      this.billForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    const payload: BillRequest = {
      patientId: Number(this.billForm.value.patientId),
      appointmentId: this.billForm.value.appointmentId ? Number(this.billForm.value.appointmentId) : null,
      consultationFee: Number(this.billForm.value.consultationFee) || 0,
      medicineCharges: Number(this.billForm.value.medicineCharges) || 0,
      testCharges: Number(this.billForm.value.testCharges) || 0,
      otherCharges: Number(this.billForm.value.otherCharges) || 0,
      paymentStatus: this.billForm.value.paymentStatus,
      paymentMethod: this.billForm.value.paymentMethod || undefined,
      billDate: this.billForm.value.billDate,
      notes: this.billForm.value.notes
    };

    if (this.isEditMode && this.billId) {
      this.billService.update(this.billId, payload).subscribe({
        next: (res) => {
          this.isSubmitting = false;
          this.router.navigate(['/bills', res.id]);
        },
        error: (err) => {
          this.errorMessage = 'Failed to update bill: ' + (err.error?.message || err.message);
          this.isSubmitting = false;
        }
      });
    } else {
      this.billService.create(payload).subscribe({
        next: (res) => {
          this.isSubmitting = false;
          this.router.navigate(['/bills', res.id]);
        },
        error: (err) => {
          this.errorMessage = 'Failed to create bill: ' + (err.error?.message || err.message);
          this.isSubmitting = false;
        }
      });
    }
  }
}
