package com.hospital.hms.service;

import com.hospital.hms.dto.BillRequestDTO;
import com.hospital.hms.dto.BillResponseDTO;
import com.hospital.hms.dto.PaymentUpdateDTO;
import com.hospital.hms.entity.PaymentStatus;

import java.time.LocalDate;
import java.util.List;

public interface BillService {

    BillResponseDTO createBill(BillRequestDTO request, Long authenticatedUserId);

    BillResponseDTO updateBill(Long id, BillRequestDTO request, Long authenticatedUserId);

    BillResponseDTO getBillById(Long id, Long authenticatedUserId);

    List<BillResponseDTO> getPatientBills(Long patientId, Long authenticatedUserId);

    BillResponseDTO markBillAsPaid(Long id, PaymentUpdateDTO paymentDTO, Long authenticatedUserId);

    List<BillResponseDTO> searchAndFilterBills(String search, Long patientId, LocalDate date, PaymentStatus paymentStatus, Long authenticatedUserId);

    List<BillResponseDTO> getAllBills(Long authenticatedUserId);
}
