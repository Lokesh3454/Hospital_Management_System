export type AppointmentStatus = 'BOOKED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'RESCHEDULED';

export interface Appointment {
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
  doctorConsultationFee?: number;
  appointmentDate: string;
  appointmentTime: string;
  formattedTime?: string;
  status: AppointmentStatus;
  reason: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AppointmentRequest {
  patientId?: number;
  doctorId: number;
  appointmentDate: string;
  appointmentTime: string;
  reason: string;
  notes?: string;
}

export interface RescheduleRequest {
  appointmentDate: string;
  appointmentTime: string;
  reason?: string;
  notes?: string;
}

export interface TimeSlot {
  time: string;
  formattedTime: string;
  available: boolean;
  booked: boolean;
}
