export interface BloodInventory {
  id?: number;
  bloodGroup: string; // 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'
  componentType: 'WHOLE_BLOOD' | 'PRBC' | 'PLATELETS' | 'FFP';
  unitsAvailable: number;
  reservedUnits?: number;
  expiryDate: string;
  storageLocation?: string;
  lastRestockedAt?: string;
}
