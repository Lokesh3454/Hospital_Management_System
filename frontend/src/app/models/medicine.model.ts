export type DosageForm = 'TABLET' | 'CAPSULE' | 'SYRUP' | 'INJECTION' | 'OINTMENT' | 'DROPS' | 'INHALER';

export interface Medicine {
  id?: number;
  name: string;
  genericName?: string;
  category?: string;
  dosageForm?: DosageForm;
  strength?: string;
  manufacturer?: string;
  batchNumber?: string;
  unitPrice: number;
  stockQuantity: number;
  reorderLevel: number;
  expiryDate?: string;
  isLowStock?: boolean;
  isExpired?: boolean;
}
