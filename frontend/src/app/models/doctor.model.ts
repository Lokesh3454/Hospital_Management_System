export type DoctorStatus = 'AVAILABLE' | 'ON_LEAVE' | 'BUSY' | 'INACTIVE';

export interface Doctor {
  doctorId?: number;
  name: string;
  firstName?: string;
  lastName?: string;
  email: string;
  phone: string;
  specialization: string;
  qualification?: string;
  experience?: number;
  consultationFee: number;
  status: DoctorStatus;
  department?: string;
  roomNumber?: string;
  availableDays?: string;
  createdAt?: string;
}
