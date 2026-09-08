export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  roles: string[];
  profileId?: number;
}

export interface PatientProfile {
  id: number;
  userId: number;
  dateOfBirth?: string;
  gender?: string;
  bloodGroup?: string;
  address?: string;
  emergencyContact?: string;
}

export interface DoctorProfile {
  id: number;
  userId: number;
  specialization: string;
  qualification?: string;
  experienceYears?: number;
  consultationFee: number;
  department?: string;
  roomNumber?: string;
  availableDays?: string;
}
