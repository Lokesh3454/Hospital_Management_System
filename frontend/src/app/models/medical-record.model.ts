export interface MedicalRecord {
  id?: number;
  patientId: number;
  patientName?: string;
  patientPhone?: string;
  patientEmail?: string;
  patientGender?: string;
  patientBloodGroup?: string;
  doctorId: number;
  doctorName?: string;
  doctorSpecialization?: string;
  doctorDepartment?: string;
  appointmentId?: number;
  appointmentReason?: string;
  diagnosis: string;
  symptoms?: string;
  treatment?: string;
  testResults?: string;
  notes?: string;
  recordDate: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface MedicalRecordRequest {
  patientId: number;
  doctorId?: number;
  appointmentId?: number;
  diagnosis: string;
  symptoms?: string;
  treatment?: string;
  testResults?: string;
  notes?: string;
  recordDate?: string;
}
