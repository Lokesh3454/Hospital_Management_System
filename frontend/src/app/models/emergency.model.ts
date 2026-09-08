export interface EmergencyCase {
  id?: number;
  caseNumber: string;
  patientName: string;
  patientAge?: number;
  gender?: string;
  triageLevel: 'RESUSCITATION' | 'EMERGENT' | 'URGENT' | 'LESS_URGENT' | 'NON_URGENT';
  chiefComplaint?: string;
  heartRate?: number;
  bloodPressure?: string;
  spo2?: number;
  temperature?: number;
  status: 'TRIAGED' | 'IN_TREATMENT' | 'ADMITTED' | 'DISCHARGED';
  assignedDoctorName?: string;
  assignedBedNumber?: string;
  ambulanceNumber?: string;
  etaMinutes?: number;
  codeBlueTriggered?: boolean;
  arrivalTime?: string;
  createdAt?: string;
}
