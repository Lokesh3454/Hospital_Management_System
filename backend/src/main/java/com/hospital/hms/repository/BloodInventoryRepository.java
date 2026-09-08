package com.hospital.hms.repository;

import com.hospital.hms.entity.BloodInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {
    Optional<BloodInventory> findByBloodGroupAndComponentType(String bloodGroup, BloodInventory.ComponentType componentType);
    List<BloodInventory> findByBloodGroup(String bloodGroup);
    List<BloodInventory> findByExpiryDateBefore(LocalDate date);
}
