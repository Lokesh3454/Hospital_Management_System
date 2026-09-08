export interface Patient {
  id?: number;
  patientId?: number;
  name: string;
  firstName?: string;
  lastName?: string;
  dateOfBirth?: string;
  gender?: string;
  phone: string;
  email: string;
  address?: string;
  bloodGroup?: string;
  emergencyContact?: string;
  allergies?: string;
  chronicConditions?: string;
  createdAt?: string;
}
