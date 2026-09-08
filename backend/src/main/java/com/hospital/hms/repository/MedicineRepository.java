package com.hospital.hms.repository;

import com.hospital.hms.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByNameContainingIgnoreCaseOrGenericNameContainingIgnoreCase(String name, String genericName);

    @Query("SELECT m FROM Medicine m WHERE m.stockQuantity <= m.reorderLevel")
    List<Medicine> findLowStockMedicines();

    List<Medicine> findByExpiryDateBefore(LocalDate date);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.stockQuantity <= m.reorderLevel")
    long countLowStock();
}
