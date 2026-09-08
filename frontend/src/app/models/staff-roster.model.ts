export interface StaffShift {
  id?: number;
  staffName: string;
  roleTitle: string;
  department: string;
  dayOfWeek: string;
  shiftType: 'MORNING' | 'EVENING' | 'NIGHT';
  startTime: string;
  endTime: string;
  onCall?: boolean;
  contactPhone?: string;
  createdAt?: string;
}
