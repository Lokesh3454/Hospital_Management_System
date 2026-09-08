import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { AdminDashboardComponent } from './pages/dashboards/admin-dashboard/admin-dashboard.component';
import { DoctorDashboardComponent } from './pages/dashboards/doctor-dashboard/doctor-dashboard.component';
import { PatientDashboardComponent } from './pages/dashboards/patient-dashboard/patient-dashboard.component';
import { ReceptionistDashboardComponent } from './pages/dashboards/receptionist-dashboard/receptionist-dashboard.component';
import { PatientListComponent } from './pages/patients/patient-list/patient-list.component';
import { PatientFormComponent } from './pages/patients/patient-form/patient-form.component';
import { PatientDetailsComponent } from './pages/patients/patient-details/patient-details.component';
import { DoctorListComponent } from './pages/doctors/doctor-list/doctor-list.component';
import { DoctorFormComponent } from './pages/doctors/doctor-form/doctor-form.component';
import { DoctorDetailsComponent } from './pages/doctors/doctor-details/doctor-details.component';
import { DoctorAvailabilityComponent } from './pages/doctors/doctor-availability/doctor-availability.component';
import { AppointmentListComponent } from './pages/appointments/appointment-list/appointment-list.component';
import { BookAppointmentComponent } from './pages/appointments/book-appointment/book-appointment.component';
import { AppointmentDetailsComponent } from './pages/appointments/appointment-details/appointment-details.component';
import { MedicalRecordListComponent } from './pages/medical-records/medical-record-list/medical-record-list.component';
import { MedicalRecordFormComponent } from './pages/medical-records/medical-record-form/medical-record-form.component';
import { MedicalRecordDetailsComponent } from './pages/medical-records/medical-record-details/medical-record-details.component';
import { PrescriptionListComponent } from './pages/prescriptions/prescription-list/prescription-list.component';
import { PrescriptionFormComponent } from './pages/prescriptions/prescription-form/prescription-form.component';
import { PrescriptionDetailsComponent } from './pages/prescriptions/prescription-details/prescription-details.component';
import { PrescriptionPrintComponent } from './pages/prescriptions/prescription-print/prescription-print.component';
import { BillListComponent } from './pages/bills/bill-list/bill-list.component';
import { BillFormComponent } from './pages/bills/bill-form/bill-form.component';
import { BillDetailsComponent } from './pages/bills/bill-details/bill-details.component';
import { BillPrintComponent } from './pages/bills/bill-print/bill-print.component';
import { QueueDisplayComponent } from './pages/queue/queue-display/queue-display.component';
import { BedManagementComponent } from './pages/beds/bed-management/bed-management.component';
import { PharmacyInventoryComponent } from './pages/pharmacy/pharmacy-inventory/pharmacy-inventory.component';
import { LabManagementComponent } from './pages/lab/lab-management/lab-management.component';
import { TelemedicineRoomComponent } from './pages/telemedicine/telemedicine-room/telemedicine-room.component';
import { AnalyticsDashboardComponent } from './pages/analytics/analytics-dashboard/analytics-dashboard.component';
import { OperationsHubComponent } from './pages/operations/operations-hub/operations-hub.component';
import { EmergencyHubComponent } from './pages/emergency/emergency-hub/emergency-hub.component';
import { SurgeryManagementComponent } from './pages/surgery/surgery-management/surgery-management.component';
import { BloodBankComponent } from './pages/blood-bank/blood-bank-inventory/blood-bank-inventory.component';
import { InsuranceClaimsComponent } from './pages/insurance/insurance-claims/insurance-claims.component';
import { StaffRosterComponent } from './pages/staff/staff-roster/staff-roster.component';
import { NotificationLogComponent } from './pages/notifications/notification-log/notification-log.component';
import { DashboardRedirectComponent } from './pages/dashboards/dashboard-redirect.component';
import { UnauthorizedComponent } from './pages/unauthorized/unauthorized.component';
import { NotFoundComponent } from './pages/not-found/not-found.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // Role Dashboards & Central Resolver
  {
    path: 'dashboard',
    component: DashboardRedirectComponent,
    canActivate: [authGuard]
  },
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN'] }
  },
  {
    path: 'doctor',
    component: DoctorDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_DOCTOR'] }
  },
  {
    path: 'patient',
    component: PatientDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_PATIENT'] }
  },
  {
    path: 'receptionist',
    component: ReceptionistDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_RECEPTIONIST'] }
  },
  { path: 'dashboard/admin', redirectTo: 'admin', pathMatch: 'full' },
  { path: 'dashboard/doctor', redirectTo: 'doctor', pathMatch: 'full' },
  { path: 'dashboard/patient', redirectTo: 'patient', pathMatch: 'full' },
  { path: 'dashboard/receptionist', redirectTo: 'receptionist', pathMatch: 'full' },

  // Patient Management Routes
  {
    path: 'patients',
    component: PatientListComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST'] }
  },
  {
    path: 'patients/new',
    component: PatientFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_RECEPTIONIST'] }
  },
  {
    path: 'patients/:id',
    component: PatientDetailsComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_RECEPTIONIST', 'ROLE_PATIENT'] }
  },
  {
    path: 'patients/:id/edit',
    component: PatientFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_PATIENT'] }
  },

  // Doctor Management Routes
  {
    path: 'doctors',
    component: DoctorListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'doctors/new',
    component: DoctorFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN'] }
  },
  {
    path: 'doctors/:id',
    component: DoctorDetailsComponent,
    canActivate: [authGuard]
  },
  {
    path: 'doctors/:id/edit',
    component: DoctorFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] }
  },
  {
    path: 'doctors/:id/availability',
    component: DoctorAvailabilityComponent,
    canActivate: [authGuard]
  },

  // Appointment Management Routes (Phase 3)
  {
    path: 'appointments',
    component: AppointmentListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'appointments/book',
    component: BookAppointmentComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_RECEPTIONIST', 'ROLE_PATIENT'] }
  },
  { path: 'appointments/new', redirectTo: 'appointments/book', pathMatch: 'full' },
  {
    path: 'appointments/:id',
    component: AppointmentDetailsComponent,
    canActivate: [authGuard]
  },

  // Medical Records Routes (Phase 4)
  {
    path: 'medical-records',
    component: MedicalRecordListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'medical-records/new',
    component: MedicalRecordFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] }
  },
  {
    path: 'medical-records/:id',
    component: MedicalRecordDetailsComponent,
    canActivate: [authGuard]
  },
  {
    path: 'medical-records/:id/edit',
    component: MedicalRecordFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] }
  },

  // Prescriptions Routes (Phase 4)
  {
    path: 'prescriptions',
    component: PrescriptionListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'prescriptions/new',
    component: PrescriptionFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] }
  },
  {
    path: 'prescriptions/:id',
    component: PrescriptionDetailsComponent,
    canActivate: [authGuard]
  },
  {
    path: 'prescriptions/:id/edit',
    component: PrescriptionFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_DOCTOR'] }
  },
  {
    path: 'prescriptions/:id/print',
    component: PrescriptionPrintComponent,
    canActivate: [authGuard]
  },

  // Billing & Invoicing Routes (Phase 5)
  {
    path: 'bills',
    component: BillListComponent,
    canActivate: [authGuard]
  },
  {
    path: 'bills/new',
    component: BillFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_RECEPTIONIST'] }
  },
  {
    path: 'bills/:id',
    component: BillDetailsComponent,
    canActivate: [authGuard]
  },
  {
    path: 'bills/:id/edit',
    component: BillFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ROLE_ADMIN', 'ROLE_RECEPTIONIST'] }
  },
  {
    path: 'bills/:id/print',
    component: BillPrintComponent,
    canActivate: [authGuard]
  },
  {
    path: 'bills/patient/:patientId',
    component: BillListComponent,
    canActivate: [authGuard]
  },

  // Modern Healthcare Enterprise Routes
  {
    path: 'operations',
    component: OperationsHubComponent,
    canActivate: [authGuard]
  },
  { path: 'operation', redirectTo: 'operations', pathMatch: 'full' },
  { path: 'hospital-operations', redirectTo: 'operations', pathMatch: 'full' },
  { path: 'hospital-operation', redirectTo: 'operations', pathMatch: 'full' },
  {
    path: 'queue-display',
    component: QueueDisplayComponent
  },
  { path: 'queue', redirectTo: 'queue-display', pathMatch: 'full' },
  { path: 'opd-queue', redirectTo: 'queue-display', pathMatch: 'full' },
  {
    path: 'beds',
    component: BedManagementComponent,
    canActivate: [authGuard]
  },
  { path: 'bed', redirectTo: 'beds', pathMatch: 'full' },
  { path: 'wards', redirectTo: 'beds', pathMatch: 'full' },
  { path: 'ward', redirectTo: 'beds', pathMatch: 'full' },
  {
    path: 'pharmacy',
    component: PharmacyInventoryComponent,
    canActivate: [authGuard]
  },
  { path: 'medicines', redirectTo: 'pharmacy', pathMatch: 'full' },
  { path: 'medicine', redirectTo: 'pharmacy', pathMatch: 'full' },
  {
    path: 'lab',
    component: LabManagementComponent,
    canActivate: [authGuard]
  },
  { path: 'labs', redirectTo: 'lab', pathMatch: 'full' },
  { path: 'laboratory', redirectTo: 'lab', pathMatch: 'full' },
  {
    path: 'telemedicine',
    redirectTo: 'telemedicine/1',
    pathMatch: 'full'
  },
  {
    path: 'telemedicine/:appointmentId',
    component: TelemedicineRoomComponent,
    canActivate: [authGuard]
  },
  {
    path: 'analytics',
    component: AnalyticsDashboardComponent,
    canActivate: [authGuard]
  },
  { path: 'reports', redirectTo: 'analytics', pathMatch: 'full' },
  { path: 'report', redirectTo: 'analytics', pathMatch: 'full' },
  { path: 'records', redirectTo: 'medical-records', pathMatch: 'full' },
  { path: 'billing', redirectTo: 'bills', pathMatch: 'full' },
  { path: 'invoices', redirectTo: 'bills', pathMatch: 'full' },
  { path: 'invoice', redirectTo: 'bills', pathMatch: 'full' },

  // Enterprise Feature Routes
  {
    path: 'emergency',
    component: EmergencyHubComponent,
    canActivate: [authGuard]
  },
  { path: 'er', redirectTo: 'emergency', pathMatch: 'full' },
  { path: 'trauma', redirectTo: 'emergency', pathMatch: 'full' },
  {
    path: 'surgery',
    component: SurgeryManagementComponent,
    canActivate: [authGuard]
  },
  { path: 'surgeries', redirectTo: 'surgery', pathMatch: 'full' },
  { path: 'ot', redirectTo: 'surgery', pathMatch: 'full' },
  { path: 'operation-theater', redirectTo: 'surgery', pathMatch: 'full' },
  {
    path: 'blood-bank',
    component: BloodBankComponent,
    canActivate: [authGuard]
  },
  { path: 'bloodbank', redirectTo: 'blood-bank', pathMatch: 'full' },
  { path: 'blood', redirectTo: 'blood-bank', pathMatch: 'full' },
  {
    path: 'insurance',
    component: InsuranceClaimsComponent,
    canActivate: [authGuard]
  },
  { path: 'claims', redirectTo: 'insurance', pathMatch: 'full' },
  { path: 'tpa', redirectTo: 'insurance', pathMatch: 'full' },
  {
    path: 'staff-roster',
    component: StaffRosterComponent,
    canActivate: [authGuard]
  },
  { path: 'roster', redirectTo: 'staff-roster', pathMatch: 'full' },
  { path: 'duty-roster', redirectTo: 'staff-roster', pathMatch: 'full' },
  { path: 'shifts', redirectTo: 'staff-roster', pathMatch: 'full' },
  {
    path: 'notifications',
    component: NotificationLogComponent,
    canActivate: [authGuard]
  },
  { path: 'outbox', redirectTo: 'notifications', pathMatch: 'full' },
  { path: 'notification-log', redirectTo: 'notifications', pathMatch: 'full' },

  { path: 'unauthorized', component: UnauthorizedComponent },
  { path: '**', component: NotFoundComponent }
];
