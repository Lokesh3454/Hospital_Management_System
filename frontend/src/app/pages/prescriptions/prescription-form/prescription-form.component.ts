import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PrescriptionService } from '../../../core/services/prescription.service';
import { PatientService } from '../../../core/services/patient.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { AuthService } from '../../../core/services/auth.service';
import { ClinicalSupportService } from '../../../core/services/clinical-support.service';
import { Patient } from '../../../models/patient.model';
import { Doctor } from '../../../models/doctor.model';
import { PrescriptionItem } from '../../../models/prescription.model';
import { SafetyAlert } from '../../../models/clinical-support.model';

@Component({
  selector: 'app-prescription-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './prescription-form.component.html',
  styleUrls: ['./prescription-form.component.css']
})
export class PrescriptionFormComponent implements OnInit {
  prescriptionForm!: FormGroup;
  isEditMode = false;
  prescriptionId: number | null = null;
  patients: Patient[] = [];
  doctors: Doctor[] = [];

  // AI Drug-Drug Interaction (DDI) & Allergy Safety
  safetyAlerts: SafetyAlert[] = [];
  isCheckingSafety = false;
  safetyChecked = false;

  isLoading = false;
  isSubmitting = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  clinicalPresets = [
    {
      id: 'hypertension',
      name: 'Cardiology: Essential Hypertension & BP Regimen',
      notes: 'Essential Hypertension Stage 1. Monitor BP weekly. Low-sodium diet (<2g/day).',
      items: [
        { medicineName: 'Telmisartan', dosage: '40mg', frequency: 'Once daily (Morning)', duration: '90 days', instructions: 'Take with water before breakfast. Maintain daily BP log.' },
        { medicineName: 'Amlodipine Besylate', dosage: '5mg', frequency: 'Once daily (Morning)', duration: '90 days', instructions: 'Take alongside Telmisartan for synergy.' },
        { medicineName: 'Rosuvastatin Calcium', dosage: '10mg', frequency: 'Once daily (Bedtime)', duration: '90 days', instructions: 'Cardioprotective lipid management.' }
      ]
    },
    {
      id: 'diabetes',
      name: 'Endocrinology: Type 2 Diabetes Glycemic Control',
      notes: 'Type 2 Diabetes Mellitus with metabolic syndrome. Daily brisk walk and carbohydrate restriction.',
      items: [
        { medicineName: 'Metformin Hydrochloride', dosage: '500mg', frequency: 'Twice daily with meals (BID)', duration: '90 days', instructions: 'Take with breakfast and dinner to avoid gastric upset.' },
        { medicineName: 'Glimepiride', dosage: '1mg', frequency: 'Once daily (Before Breakfast)', duration: '90 days', instructions: 'Take 15 minutes prior to morning meal.' },
        { medicineName: 'Vitamin B12 (Methylcobalamin)', dosage: '1000mcg', frequency: 'Once daily (Morning)', duration: '90 days', instructions: 'Diabetic peripheral nerve support.' }
      ]
    },
    {
      id: 'asthma',
      name: 'Pulmonology: Bronchial Asthma & Airway Maintenance',
      notes: 'Moderate Persistent Bronchial Asthma. Metered-dose inhaler spacer technique demonstrated.',
      items: [
        { medicineName: 'Budesonide/Formoterol (Symbicort)', dosage: '160/4.5mcg', frequency: '2 puffs twice daily (BID)', duration: '60 days', instructions: 'Rinse mouth thoroughly with water and spit out after each use.' },
        { medicineName: 'Albuterol Sulfate Inhaler', dosage: '90mcg', frequency: '2 puffs every 4-6 hrs PRN', duration: '30 days', instructions: 'Rescue inhaler for sudden wheezing or dyspnea.' },
        { medicineName: 'Montelukast Sodium', dosage: '10mg', frequency: '1 tablet once daily at bedtime', duration: '60 days', instructions: 'Prevents nocturnal airway bronchoconstriction.' }
      ]
    },
    {
      id: 'migraine',
      name: 'Neurology: Acute Migraine & Neuro Prophylaxis',
      notes: 'Migraine with Visual Aura. Headache diary maintenance and trigger avoidance advised.',
      items: [
        { medicineName: 'Sumatriptan Succinate', dosage: '50mg', frequency: '1 tablet at onset of aura', duration: '15 days', instructions: 'Take immediately when aura starts. May repeat after 2h if needed.' },
        { medicineName: 'Propranolol Hydrochloride', dosage: '40mg', frequency: 'Once daily (Morning)', duration: '60 days', instructions: 'Migraine prophylactic beta-blockade.' },
        { medicineName: 'Magnesium Glycinate', dosage: '400mg', frequency: 'Once daily (Bedtime)', duration: '60 days', instructions: 'Reduces neurovascular excitability.' }
      ]
    },
    {
      id: 'sciatica',
      name: 'Orthopedics: Lumbar Disc / Sciatica Pain Regimen',
      notes: 'L5-S1 lumbar disc bulge with left radiculopathy. Lumbar core stabilization exercises recommended.',
      items: [
        { medicineName: 'Meloxicam', dosage: '15mg', frequency: 'Once daily with meals', duration: '14 days', instructions: 'NSAID anti-inflammatory for nerve irritation.' },
        { medicineName: 'Cyclobenzaprine', dosage: '10mg', frequency: '1 tablet at bedtime PRN', duration: '10 days', instructions: 'Muscle relaxant for paraspinal spasm. Causes drowsiness.' },
        { medicineName: 'Pantoprazole Sodium', dosage: '40mg', frequency: 'Once daily before breakfast', duration: '14 days', instructions: 'Gastric protection during NSAID course.' }
      ]
    },
    {
      id: 'gastro',
      name: 'Gastroenterology: Acute Gastroenteritis & Hydration',
      notes: 'Acute Infectious Gastroenteritis with mild dehydration. Bland BRAT diet advised.',
      items: [
        { medicineName: 'Ciprofloxacin', dosage: '500mg', frequency: 'Twice daily (BID)', duration: '5 days', instructions: 'Complete entire 5-day antibiotic course.' },
        { medicineName: 'Ondansetron ODT', dosage: '4mg', frequency: 'Every 8 hours PRN', duration: '5 days', instructions: 'Dissolve on tongue for acute nausea control.' },
        { medicineName: 'Oral Rehydration Salts (ORS)', dosage: '1 sachet in 1L water', frequency: 'Sip throughout the day', duration: '5 days', instructions: 'Electrolyte rehydration.' }
      ]
    }
  ];

  applyPreset(presetId: string): void {
    const preset = this.clinicalPresets.find(p => p.id === presetId);
    if (!preset) return;

    this.items.clear();
    for (const item of preset.items) {
      this.items.push(this.createItemFormGroup(item as any));
    }
    if (preset.notes) {
      this.prescriptionForm.patchValue({ notes: preset.notes });
    }
    this.successMessage = `Loaded clinical preset: "${preset.name}".`;
    setTimeout(() => this.successMessage = null, 4000);
  }

  constructor(
    private fb: FormBuilder,
    private prescriptionService: PrescriptionService,
    private patientService: PatientService,
    private doctorService: DoctorService,
    private clinicalSupportService: ClinicalSupportService,
    public authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const today = new Date().toISOString().split('T')[0];

    this.prescriptionForm = this.fb.group({
      patientId: ['', [Validators.required]],
      doctorId: ['', [Validators.required]],
      appointmentId: [null],
      prescriptionDate: [today, [Validators.required]],
      notes: [''],
      items: this.fb.array([])
    });

    this.loadDropdowns();

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.prescriptionId = Number(idParam);
      this.loadPrescription(this.prescriptionId);
    } else {
      // Add default 1 item row
      this.addItem();

      // Read query params from appointment page
      this.route.queryParams.subscribe(params => {
        if (params['patientId']) {
          this.prescriptionForm.patchValue({ patientId: Number(params['patientId']) });
        }
        if (params['doctorId']) {
          this.prescriptionForm.patchValue({ doctorId: Number(params['doctorId']) });
        }
        if (params['appointmentId']) {
          this.prescriptionForm.patchValue({ appointmentId: Number(params['appointmentId']) });
        }
      });
    }
  }

  get selectedPatient(): Patient | undefined {
    const pid = this.prescriptionForm?.get('patientId')?.value;
    if (!pid) return undefined;
    return this.patients.find(p => (p.id == pid || p.patientId == pid));
  }

  get items(): FormArray {
    return this.prescriptionForm.get('items') as FormArray;
  }

  createItemFormGroup(item?: PrescriptionItem): FormGroup {
    return this.fb.group({
      medicineName: [item?.medicineName || '', [Validators.required]],
      dosage: [item?.dosage || '', [Validators.required]],
      frequency: [item?.frequency || '', [Validators.required]],
      duration: [item?.duration || '', [Validators.required]],
      instructions: [item?.instructions || '']
    });
  }

  addItem(): void {
    this.items.push(this.createItemFormGroup());
  }

  removeItem(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
    }
  }

  loadDropdowns(): void {
    this.patientService.getAll().subscribe({
      next: (res) => {
        this.patients = res.data || [];
      }
    });

    this.doctorService.getAll().subscribe({
      next: (res) => {
        this.doctors = (res.data || []).filter(d => d.status !== 'INACTIVE');
        const user = this.authService.currentUserValue;
        if (user?.roles?.includes('ROLE_DOCTOR') && user.profileId) {
          this.prescriptionForm.patchValue({ doctorId: user.profileId });
        }
      }
    });
  }

  loadPrescription(id: number): void {
    this.isLoading = true;
    this.prescriptionService.getById(id).subscribe({
      next: (res) => {
        const rx = res.data;
        if (rx) {
          this.prescriptionForm.patchValue({
            patientId: rx.patientId,
            doctorId: rx.doctorId,
            appointmentId: rx.appointmentId,
            prescriptionDate: rx.prescriptionDate,
            notes: rx.notes
          });

          this.items.clear();
          if (rx.items && rx.items.length > 0) {
            rx.items.forEach(item => this.items.push(this.createItemFormGroup(item)));
          } else {
            this.addItem();
          }
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load prescription.';
        this.isLoading = false;
      }
    });
  }

  checkDrugSafety(): void {
    const medNames = (this.items.value || [])
      .map((i: any) => i.medicineName)
      .filter((n: string) => n && n.trim().length > 0);

    if (medNames.length === 0) {
      alert('Please add at least one medication name before running drug safety analysis.');
      return;
    }

    const patientId = Number(this.prescriptionForm.value.patientId);
    const selectedPat = this.patients.find(p => (p.patientId || p.id) === patientId);
    const patientAllergies = selectedPat?.allergies || '';

    this.isCheckingSafety = true;
    this.clinicalSupportService.checkSafety({
      medicineNames: medNames,
      patientAllergies: patientAllergies,
      patientId: patientId || undefined
    }).subscribe({
      next: (res) => {
        this.isCheckingSafety = false;
        this.safetyChecked = true;
        this.safetyAlerts = res.data || [];
        if (this.safetyAlerts.length === 0) {
          this.successMessage = 'AI Drug Safety Check: All clear! No interactions or allergy contraindications detected.';
          setTimeout(() => this.successMessage = null, 4000);
        }
      },
      error: () => {
        this.isCheckingSafety = false;
      }
    });
  }

  onSubmit(): void {
    if (this.prescriptionForm.invalid) {
      this.prescriptionForm.markAllAsTouched();
      this.errorMessage = 'Please complete all required fields and medication details.';
      return;
    }

    if (this.items.length === 0) {
      this.errorMessage = 'Please add at least one medication to the prescription.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    const formVal = this.prescriptionForm.value;
    const payload = {
      patientId: Number(formVal.patientId),
      doctorId: Number(formVal.doctorId),
      appointmentId: formVal.appointmentId ? Number(formVal.appointmentId) : undefined,
      prescriptionDate: formVal.prescriptionDate,
      notes: formVal.notes,
      items: formVal.items
    };

    if (this.isEditMode && this.prescriptionId) {
      this.prescriptionService.update(this.prescriptionId, payload).subscribe({
        next: () => {
          this.successMessage = 'Prescription updated successfully!';
          this.isSubmitting = false;
          setTimeout(() => this.router.navigate(['/prescriptions', this.prescriptionId]), 1200);
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to update prescription.';
          this.isSubmitting = false;
        }
      });
    } else {
      this.prescriptionService.create(payload).subscribe({
        next: (res) => {
          this.successMessage = 'Prescription issued successfully!';
          this.isSubmitting = false;
          const newId = res.data ? res.data.id : null;
          setTimeout(() => {
            if (newId) {
              this.router.navigate(['/prescriptions', newId]);
            } else {
              this.router.navigate(['/prescriptions']);
            }
          }, 1200);
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to issue prescription.';
          this.isSubmitting = false;
        }
      });
    }
  }
}
