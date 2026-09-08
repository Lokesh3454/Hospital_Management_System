export interface SafetyCheckRequest {
  medicineNames: string[];
  patientAllergies?: string;
  patientId?: number;
}

export interface SafetyAlert {
  severity: 'HIGH' | 'MODERATE' | 'LOW';
  type: 'DRUG_INTERACTION' | 'ALLERGY_CONTRAINDICATION' | 'DOSAGE_WARNING';
  title: string;
  description: string;
  recommendation: string;
}

export interface ClinicalBrief {
  patientName: string;
  ageGender: string;
  bloodGroup: string;
  chronicConditions: string[];
  activeAllergies: string[];
  latestVitalsSummary: string;
  clinicalHighlights: string[];
}
