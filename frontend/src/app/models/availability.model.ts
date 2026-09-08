export interface DoctorAvailability {
  availabilityId?: number;
  doctorId?: number;
  doctorName?: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  available: boolean;
}
