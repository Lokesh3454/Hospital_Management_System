export interface InsuranceClaim {
  id?: number;
  claimNumber: string;
  patientName: string;
  patientId?: number;
  billId?: number;
  policyNumber: string;
  insuranceProvider: string;
  tpaName?: string;
  totalBillAmount: number;
  claimAmount: number;
  approvedAmount?: number;
  patientCoPay?: number;
  status: 'SUBMITTED' | 'PRE_AUTH_APPROVED' | 'UNDER_REVIEW' | 'SETTLED' | 'REJECTED';
  icdCode?: string;
  claimNotes?: string;
  submissionDate?: string;
  settlementDate?: string;
  createdAt?: string;
}
