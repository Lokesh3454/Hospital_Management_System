package com.hospital.hms.service.impl;

import com.hospital.hms.dto.BillRequestDTO;
import com.hospital.hms.dto.BillResponseDTO;
import com.hospital.hms.dto.PaymentUpdateDTO;
import com.hospital.hms.entity.*;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.*;
import com.hospital.hms.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillServiceImpl implements BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public BillResponseDTO createBill(BillRequestDTO request, Long authenticatedUserId) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + request.getAppointmentId()));
        }

        Bill bill = new Bill();
        bill.setPatient(patient);
        bill.setAppointment(appointment);
        bill.setConsultationFee(request.getConsultationFee() != null ? request.getConsultationFee() : BigDecimal.ZERO);
        bill.setMedicineCharges(request.getMedicineCharges() != null ? request.getMedicineCharges() : BigDecimal.ZERO);
        bill.setTestCharges(request.getTestCharges() != null ? request.getTestCharges() : BigDecimal.ZERO);
        bill.setOtherCharges(request.getOtherCharges() != null ? request.getOtherCharges() : BigDecimal.ZERO);

        bill.setPaymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : PaymentStatus.PENDING);
        bill.setPaymentMethod(request.getPaymentMethod());
        bill.setNotes(request.getNotes());

        if (request.getBillDate() != null) {
            bill.setBillingDate(request.getBillDate().atTime(LocalDateTime.now().toLocalTime()));
        } else {
            bill.setBillingDate(LocalDateTime.now());
        }

        String billNum = "BILL-" + Year.now().getValue() + "-" + String.format("%05d", (billRepository.count() + 1));
        bill.setBillNumber(billNum);

        bill.calculateTotalAmount();
        Bill saved = billRepository.save(bill);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public BillResponseDTO updateBill(Long id, BillRequestDTO request, Long authenticatedUserId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));

        if (request.getPatientId() != null && !request.getPatientId().equals(bill.getPatient().getId())) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
            bill.setPatient(patient);
        }

        if (request.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + request.getAppointmentId()));
            bill.setAppointment(appointment);
        }

        if (request.getConsultationFee() != null) bill.setConsultationFee(request.getConsultationFee());
        if (request.getMedicineCharges() != null) bill.setMedicineCharges(request.getMedicineCharges());
        if (request.getTestCharges() != null) bill.setTestCharges(request.getTestCharges());
        if (request.getOtherCharges() != null) bill.setOtherCharges(request.getOtherCharges());
        if (request.getPaymentStatus() != null) bill.setPaymentStatus(request.getPaymentStatus());
        if (request.getPaymentMethod() != null) bill.setPaymentMethod(request.getPaymentMethod());
        if (request.getNotes() != null) bill.setNotes(request.getNotes());
        if (request.getBillDate() != null) {
            bill.setBillingDate(request.getBillDate().atTime(bill.getBillingDate() != null ? bill.getBillingDate().toLocalTime() : LocalDateTime.now().toLocalTime()));
        }

        bill.calculateTotalAmount();
        Bill updated = billRepository.save(bill);
        return mapToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponseDTO getBillById(Long id, Long authenticatedUserId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));

        enforcePatientPrivacy(bill, authenticatedUserId);
        return mapToDTO(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponseDTO> getPatientBills(Long patientId, Long authenticatedUserId) {
        enforcePatientAccess(patientId, authenticatedUserId);
        return billRepository.findByPatientIdOrderByBillingDateDesc(patientId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BillResponseDTO markBillAsPaid(Long id, PaymentUpdateDTO paymentDTO, Long authenticatedUserId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));

        enforcePatientPrivacy(bill, authenticatedUserId);

        if (paymentDTO.getPaymentMethod() == null || paymentDTO.getPaymentMethod().trim().isEmpty()) {
            throw new BadRequestException("Payment method is required to mark bill as paid.");
        }

        bill.setPaymentMethod(paymentDTO.getPaymentMethod().toUpperCase());
        bill.setPaymentStatus(paymentDTO.getPaymentStatus() != null ? paymentDTO.getPaymentStatus() : PaymentStatus.PAID);
        if (paymentDTO.getNotes() != null && !paymentDTO.getNotes().trim().isEmpty()) {
            bill.setNotes((bill.getNotes() != null ? bill.getNotes() + " | " : "") + paymentDTO.getNotes());
        }

        Bill saved = billRepository.save(bill);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponseDTO> searchAndFilterBills(String search, Long patientId, LocalDate date, PaymentStatus paymentStatus, Long authenticatedUserId) {
        Long scopedPatientId = patientId;

        // If user is a patient, strictly scope to their patient ID
        if (authenticatedUserId != null) {
            User user = userRepository.findById(authenticatedUserId).orElse(null);
            if (user != null && user.getRoles().stream().anyMatch(r -> r.getName().equals(RoleType.ROLE_PATIENT))) {
                Patient patient = patientRepository.findByUserId(authenticatedUserId).orElse(null);
                if (patient != null) {
                    scopedPatientId = patient.getId();
                }
            }
        }

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (date != null) {
            startDate = date.atStartOfDay();
            endDate = date.atTime(23, 59, 59);
        }

        return billRepository.searchAndFilter(search, scopedPatientId, paymentStatus, startDate, endDate)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponseDTO> getAllBills(Long authenticatedUserId) {
        return searchAndFilterBills(null, null, null, null, authenticatedUserId);
    }

    private void enforcePatientPrivacy(Bill bill, Long authenticatedUserId) {
        if (authenticatedUserId == null) return;
        User user = userRepository.findById(authenticatedUserId).orElse(null);
        if (user != null && user.getRoles().stream().anyMatch(r -> r.getName().equals(RoleType.ROLE_PATIENT))) {
            Patient patient = patientRepository.findByUserId(authenticatedUserId).orElse(null);
            if (patient == null || !patient.getId().equals(bill.getPatient().getId())) {
                throw new BadRequestException("Access denied: You can only view your own billing details.");
            }
        }
    }

    private void enforcePatientAccess(Long requestedPatientId, Long authenticatedUserId) {
        if (authenticatedUserId == null) return;
        User user = userRepository.findById(authenticatedUserId).orElse(null);
        if (user != null && user.getRoles().stream().anyMatch(r -> r.getName().equals(RoleType.ROLE_PATIENT))) {
            Patient patient = patientRepository.findByUserId(authenticatedUserId).orElse(null);
            if (patient == null || !patient.getId().equals(requestedPatientId)) {
                throw new BadRequestException("Access denied: You can only access your own bills.");
            }
        }
    }

    private BillResponseDTO mapToDTO(Bill bill) {
        BillResponseDTO dto = new BillResponseDTO();
        dto.setId(bill.getId());
        dto.setBillId(bill.getId());
        dto.setBillNumber(bill.getBillNumber());
        dto.setConsultationFee(bill.getConsultationFee());
        dto.setMedicineCharges(bill.getMedicineCharges());
        dto.setTestCharges(bill.getTestCharges());
        dto.setOtherCharges(bill.getOtherCharges());
        dto.setTotalAmount(bill.getTotalAmount());
        dto.setPaymentStatus(bill.getPaymentStatus());
        dto.setPaymentMethod(bill.getPaymentMethod());
        dto.setBillingDate(bill.getBillingDate());
        dto.setNotes(bill.getNotes());

        if (bill.getPatient() != null) {
            dto.setPatientId(bill.getPatient().getId());
            if (bill.getPatient().getUser() != null) {
                dto.setPatientName(bill.getPatient().getUser().getFirstName() + " " + bill.getPatient().getUser().getLastName());
                dto.setPatientPhone(bill.getPatient().getUser().getPhone());
                dto.setPatientEmail(bill.getPatient().getUser().getEmail());
            }
            dto.setPatientAddress(bill.getPatient().getAddress());
            dto.setPatientBloodGroup(bill.getPatient().getBloodGroup());
        }

        if (bill.getAppointment() != null) {
            dto.setAppointmentId(bill.getAppointment().getId());
            dto.setAppointmentDate(bill.getAppointment().getAppointmentDate());
            dto.setAppointmentTime(bill.getAppointment().getAppointmentTime());
            if (bill.getAppointment().getDoctor() != null) {
                if (bill.getAppointment().getDoctor().getUser() != null) {
                    dto.setDoctorName("Dr. " + bill.getAppointment().getDoctor().getUser().getFirstName() + " " + bill.getAppointment().getDoctor().getUser().getLastName());
                }
                dto.setDoctorSpecialization(bill.getAppointment().getDoctor().getSpecialization());
            }
        }

        return dto;
    }
}
