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

    @Query("SELECT b FROM Bill b JOIN FETCH b.patient p JOIN FETCH p.user pu LEFT JOIN FETCH b.appointment a LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH d.user du WHERE b.paymentStatus = :status ORDER BY b.id DESC")
    List<Bill> findTop5ByPaymentStatusWithDetails(@Param("status") PaymentStatus status, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT b FROM Bill b JOIN FETCH b.patient p JOIN FETCH p.user pu LEFT JOIN FETCH b.appointment a LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH d.user du ORDER BY b.id DESC")
    List<Bill> findTop5RecentWithDetails(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT b FROM Bill b JOIN FETCH b.patient p JOIN FETCH p.user pu LEFT JOIN FETCH b.appointment a WHERE " +
           "(:patientId IS NULL OR p.id = :patientId) AND " +
           "(:paymentStatus IS NULL OR b.paymentStatus = :paymentStatus) AND " +
           "(:startDate IS NULL OR b.billingDate >= :startDate) AND " +
           "(:endDate IS NULL OR b.billingDate <= :endDate) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(b.billNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pu.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pu.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(pu.firstName, ' ', pu.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pu.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "pu.phone LIKE CONCAT('%', :search, '%')) " +
           "ORDER BY b.billingDate DESC")
    List<Bill> searchAndFilter(
            @Param("search") String search,
            @Param("patientId") Long patientId,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
