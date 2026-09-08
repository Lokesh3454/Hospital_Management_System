export interface PrescriptionItem {
  id?: number;
  medicineName: string;
  dosage: string;
  frequency: string;
  duration: string;
  instructions?: string;
}

export interface Prescription {
  id?: number;
  patientId: number;
  patientName?: string;
  patientPhone?: string;
  patientEmail?: string;
  patientGender?: string;
  doctorId: number;
  doctorName?: string;
  doctorSpecialization?: string;
  doctorDepartment?: string;
  doctorRoomNumber?: string;
  appointmentId?: number;
  appointmentReason?: string;
  prescriptionDate: string;
  notes?: string;
  items: PrescriptionItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface PrescriptionRequest {
  patientId: number;
  doctorId?: number;
  appointmentId?: number;
  prescriptionDate?: string;
  notes?: string;
  items: PrescriptionItem[];
}
