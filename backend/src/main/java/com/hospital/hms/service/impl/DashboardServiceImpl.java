package com.hospital.hms.service.impl;

import com.hospital.hms.dto.AppointmentResponseDTO;
import com.hospital.hms.dto.BillResponseDTO;
import com.hospital.hms.dto.MedicalRecordResponseDTO;
import com.hospital.hms.dto.dashboard.*;
import com.hospital.hms.entity.*;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.*;
import com.hospital.hms.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDTO getAdminDashboard() {
        AdminDashboardDTO dto = new AdminDashboardDTO();

        dto.setTotalPatients(patientRepository.count());
        dto.setTotalDoctors(doctorRepository.count());
        dto.setTotalAppointments(appointmentRepository.count());

        long pendingBills = billRepository.countByPaymentStatus(PaymentStatus.PENDING);
        dto.setPendingBills(pendingBills);

        BigDecimal pendingAmount = billRepository.sumTotalAmountByPaymentStatus(PaymentStatus.PENDING);
        dto.setPendingBillsAmount(pendingAmount != null ? pendingAmount : BigDecimal.ZERO);

        LocalDate today = LocalDate.now();
        dto.setTodayAppointments(appointmentRepository.countByAppointmentDate(today));

        List<AppointmentResponseDTO> todayAppts = appointmentRepository
                .findByAppointmentDateOrderByAppointmentTimeAsc(today)
                .stream().map(AppointmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setTodayAppointmentsList(todayAppts);

        List<BillResponseDTO> recentBills = billRepository
                .findByPaymentStatus(PaymentStatus.PENDING)
                .stream().limit(5)
                .map(BillResponseDTO::fromEntity)
                .collect(Collectors.toList());
        if (recentBills.isEmpty()) {
            recentBills = billRepository.findAll().stream()
                    .sorted((b1, b2) -> b2.getId().compareTo(b1.getId()))
                    .limit(5)
                    .map(BillResponseDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        dto.setRecentBills(recentBills);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDashboardDTO getDoctorDashboard(Long authenticatedUserId) {
        Doctor doctor = null;
        if (authenticatedUserId != null) {
            doctor = doctorRepository.findByUserId(authenticatedUserId).orElse(null);
        }
        if (doctor == null) {
            doctor = doctorRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        }

        DoctorDashboardDTO dto = new DoctorDashboardDTO();
        LocalDate today = LocalDate.now();

        dto.setTodayAppointments(appointmentRepository.countByDoctorIdAndAppointmentDate(doctor.getId(), today));
        dto.setUpcomingAppointments(appointmentRepository.countUpcomingByDoctorId(doctor.getId(), today));
        
        long patientCount = patientRepository.countDistinctPatientsByDoctorId(doctor.getId());
        dto.setTotalPatients(patientCount);

        dto.setRecentMedicalRecords(medicalRecordRepository.countByDoctorId(doctor.getId()));
        dto.setPrescriptions(prescriptionRepository.countByDoctorId(doctor.getId()));

        List<AppointmentResponseDTO> todayList = appointmentRepository
                .findByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(doctor.getId(), today)
                .stream().map(AppointmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setTodayAppointmentsList(todayList);

        List<AppointmentResponseDTO> upcomingList = appointmentRepository
                .findUpcomingByDoctorId(doctor.getId(), today)
                .stream().limit(10)
                .map(AppointmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setUpcomingAppointmentsList(upcomingList);

        List<MedicalRecordResponseDTO> recentRecords = medicalRecordRepository
                .findTop5ByDoctorIdOrderByRecordDateDescCreatedAtDesc(doctor.getId())
                .stream().map(MedicalRecordResponseDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setRecentMedicalRecordsList(recentRecords);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDashboardDTO getPatientDashboard(Long authenticatedUserId) {
        Patient patient = null;
        if (authenticatedUserId != null) {
            patient = patientRepository.findByUserId(authenticatedUserId).orElse(null);
        }
        if (patient == null) {
            patient = patientRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        }

        PatientDashboardDTO dto = new PatientDashboardDTO();
        LocalDate today = LocalDate.now();

        // Upcoming appointment (active, not cancelled or completed)
        List<Appointment> upcoming = appointmentRepository
                .findUpcomingByPatientId(patient.getId(), today);
        if (!upcoming.isEmpty()) {
            dto.setUpcomingAppointment(AppointmentResponseDTO.fromEntity(upcoming.get(0)));
        }

        dto.setUpcomingAppointments(appointmentRepository.countUpcomingByPatientId(patient.getId(), today));
        dto.setAppointmentHistory(appointmentRepository.countHistoryByPatientId(patient.getId(), today));
        dto.setMedicalRecords(medicalRecordRepository.countByPatientId(patient.getId()));
        dto.setPrescriptions(prescriptionRepository.countByPatientId(patient.getId()));

        List<Bill> patientBills = billRepository.findByPatientIdOrderByBillingDateDesc(patient.getId());
        List<BillResponseDTO> pendingBills = patientBills.stream()
                .filter(b -> b.getPaymentStatus() == PaymentStatus.PENDING || b.getPaymentStatus() == PaymentStatus.UNPAID)
                .map(BillResponseDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setPendingBills(pendingBills.size());
        dto.setPendingBillsList(pendingBills);

        List<AppointmentResponseDTO> recentAppointments = appointmentRepository
                .findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patient.getId())
                .stream().limit(5)
                .map(AppointmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setRecentAppointmentsList(recentAppointments);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public ReceptionistDashboardDTO getReceptionistDashboard() {
        ReceptionistDashboardDTO dto = new ReceptionistDashboardDTO();
        LocalDate today = LocalDate.now();

        dto.setTodayAppointments(appointmentRepository.countByAppointmentDate(today));
        dto.setPatientRegistrations(patientRepository.count());
        dto.setAppointmentBookings(appointmentRepository.count());
        dto.setPendingBills(billRepository.countByPaymentStatus(PaymentStatus.PENDING));

        List<AppointmentResponseDTO> todayAppts = appointmentRepository
                .findByAppointmentDateOrderByAppointmentTimeAsc(today)
                .stream().map(AppointmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
        dto.setTodayAppointmentsList(todayAppts);

        List<BillResponseDTO> recentBills = billRepository
                .findByPaymentStatus(PaymentStatus.PENDING)
                .stream().limit(5)
                .map(BillResponseDTO::fromEntity)
                .collect(Collectors.toList());
        if (recentBills.isEmpty()) {
            recentBills = billRepository.findAll().stream()
                    .sorted((b1, b2) -> b2.getId().compareTo(b1.getId()))
                    .limit(5)
                    .map(BillResponseDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        dto.setRecentBillsList(recentBills);

        return dto;
    }
}
