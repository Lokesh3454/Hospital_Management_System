export type WardType = 'GENERAL' | 'SEMI_PRIVATE' | 'ICU' | 'EMERGENCY' | 'PEDIATRIC' | 'MATERNITY';
export type BedStatus = 'AVAILABLE' | 'OCCUPIED' | 'CLEANING' | 'MAINTENANCE';

export interface Bed {
  id?: number;
  bedNumber: string;
  wardType: WardType;
  roomNumber?: string;
  status: BedStatus;
  dailyRate: number;
  patientId?: number;
  patientName?: string;
  patientPhone?: string;
  admissionDate?: string;
  notes?: string;
}
