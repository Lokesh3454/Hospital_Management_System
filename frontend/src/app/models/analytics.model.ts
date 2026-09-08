export interface MonthlyTrend {
  month: string;
  revenue: number;
  appointments: number;
  patients: number;
}

export interface DoctorWorkload {
  doctorName: string;
  department: string;
  totalConsultations: number;
  completedConsultations: number;
  rating: number;
}

export interface DiagnosisStat {
  diagnosis: string;
  caseCount: number;
  percentage: number;
}

export interface HospitalAnalytics {
  totalRevenue: number;
  monthlyRevenue: number;
  totalAppointments: number;
  completedAppointments: number;
  totalPatients: number;
  totalDoctors: number;
  totalPrescriptions: number;
  totalLabOrders: number;
  totalBeds: number;
  occupiedBeds: number;
  availableBeds: number;
  bedOccupancyRate: number;
  totalMedicines: number;
  lowStockMedicines: number;
  appointmentsByDepartment: Record<string, number>;
  appointmentsByStatus: Record<string, number>;
  monthlyTrends: MonthlyTrend[];
  doctorWorkloads: DoctorWorkload[];
  topDiagnoses: DiagnosisStat[];
}
