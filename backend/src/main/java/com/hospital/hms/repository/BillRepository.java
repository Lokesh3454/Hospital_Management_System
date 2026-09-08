package com.hospital.hms.repository;

import com.hospital.hms.entity.Bill;
import com.hospital.hms.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillNumber(String billNumber);

    List<Bill> findByPatientIdOrderByBillingDateDesc(Long patientId);

    Optional<Bill> findByAppointmentId(Long appointmentId);

    List<Bill> findByPaymentStatus(PaymentStatus paymentStatus);

    long countByPaymentStatus(PaymentStatus paymentStatus);

    long countByPaymentStatusIn(List<PaymentStatus> statuses);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.paymentStatus = :status")
    BigDecimal sumTotalAmountByPaymentStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.paymentStatus = 'PAID'")
    BigDecimal sumTotalPaidRevenue();

    @Query("SELECT b FROM Bill b WHERE " +
           "(:patientId IS NULL OR b.patient.id = :patientId) AND " +
           "(:paymentStatus IS NULL OR b.paymentStatus = :paymentStatus) AND " +
           "(:startDate IS NULL OR b.billingDate >= :startDate) AND " +
           "(:endDate IS NULL OR b.billingDate <= :endDate) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(b.billNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.patient.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.patient.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(b.patient.user.firstName, ' ', b.patient.user.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.patient.user.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "b.patient.user.phone LIKE CONCAT('%', :search, '%')) " +
           "ORDER BY b.billingDate DESC")
    List<Bill> searchAndFilter(
            @Param("search") String search,
            @Param("patientId") Long patientId,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
