export type LabTestStatus = 'ORDERED' | 'SAMPLE_COLLECTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface LabTest {
  id?: number;
  testCode?: string;
  testName: string;
  category?: string;
  patientId: number;
  patientName?: string;
  doctorId?: number;
  doctorName?: string;
  orderDate?: string;
  collectionDate?: string;
  completionDate?: string;
  status: LabTestStatus;
  normalRange?: string;
  resultValue?: string;
  unit?: string;
  interpretation?: string;
  remarks?: string;
  cost?: number;
}
