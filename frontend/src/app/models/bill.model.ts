export type PaymentStatus = 'PENDING' | 'PAID' | 'CANCELLED' | 'UNPAID';
export type PaymentMethod = 'CASH' | 'CARD' | 'UPI';

export interface Bill {
  id: number;
  billId?: number;
  billNumber: string;

  patientId: number;
  patientName: string;
  patientPhone?: string;
  patientEmail?: string;
  patientAddress?: string;
  patientBloodGroup?: string;

  appointmentId?: number;
  appointmentDate?: string;
  appointmentTime?: string;
  doctorName?: string;
  doctorSpecialization?: string;

  consultationFee: number;
  medicineCharges: number;
  testCharges: number;
  otherCharges: number;
  totalAmount: number;

  paymentStatus: PaymentStatus;
  paymentMethod?: string;
  billingDate: string;
  notes?: string;
}

export interface BillRequest {
  patientId: number;
  appointmentId?: number | null;
  consultationFee: number;
  medicineCharges: number;
  testCharges: number;
  otherCharges: number;
  paymentStatus?: PaymentStatus;
  paymentMethod?: string;
  billDate?: string;
  notes?: string;
}

export interface PaymentUpdate {
  paymentMethod: string;
  paymentStatus?: PaymentStatus;
  notes?: string;
}
