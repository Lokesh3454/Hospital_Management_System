import { Appointment } from './appointment.model';
import { Bill } from './bill.model';
import { MedicalRecord } from './medical-record.model';

export interface AdminDashboard {
  totalPatients: number;
  totalDoctors: number;
  totalAppointments: number;
  pendingBills: number;
  pendingBillsAmount: number;
  todayAppointments: number;
  todayAppointmentsList: Appointment[];
  recentBills: Bill[];
}

export interface DoctorDashboard {
  todayAppointments: number;
  upcomingAppointments: number;
  totalPatients: number;
  recentMedicalRecords: number;
  prescriptions: number;
  todayAppointmentsList: Appointment[];
  upcomingAppointmentsList: Appointment[];
  recentMedicalRecordsList: MedicalRecord[];
}

export interface PatientDashboard {
  upcomingAppointment: Appointment | null;
  upcomingAppointments: number;
  appointmentHistory: number;
  medicalRecords: number;
  prescriptions: number;
  pendingBills: number;
  pendingBillsList: Bill[];
  recentAppointmentsList: Appointment[];
}

export interface ReceptionistDashboard {
  todayAppointments: number;
  patientRegistrations: number;
  appointmentBookings: number;
  pendingBills: number;
  todayAppointmentsList: Appointment[];
  recentBillsList: Bill[];
}
