export interface Vitals {
  id?: number;
  patientId: number;
  patientName?: string;
  recordedBy?: string;
  systolicBP?: number;
  diastolicBP?: number;
  heartRate?: number;
  temperature?: number;
  spo2?: number;
  respiratoryRate?: number;
  weightKg?: number;
  heightCm?: number;
  bmi?: number;
  notes?: string;
  recordedAt?: string;
}
