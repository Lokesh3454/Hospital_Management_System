export interface SurgerySchedule {
  id?: number;
  surgeryNumber: string;
  patientName: string;
  patientId?: number;
  leadSurgeonName: string;
  anesthesiologistName?: string;
  scrubNurseName?: string;
  otRoom: string;
  procedureName: string;
  surgeryDate: string;
  scheduledStartTime?: string;
  estimatedDurationHours?: number;
  status: 'SCHEDULED' | 'IN_PROGRESS' | 'IN_PACU' | 'COMPLETED' | 'CANCELLED';
  preOpCleared?: boolean;
  anesthesiaCleared?: boolean;
  consentSigned?: boolean;
  bloodReserved?: boolean;
  pacuRecoveryScore?: number;
  surgicalNotes?: string;
  createdAt?: string;
}
