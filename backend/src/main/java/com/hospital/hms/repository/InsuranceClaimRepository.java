package com.hospital.hms.repository;

import com.hospital.hms.entity.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {
    List<InsuranceClaim> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<InsuranceClaim> findAllByOrderByCreatedAtDesc();
    Optional<InsuranceClaim> findByClaimNumber(String claimNumber);
    long countByStatus(InsuranceClaim.ClaimStatus status);
}
