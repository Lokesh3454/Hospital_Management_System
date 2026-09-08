package com.hospital.hms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_inventory")
public class BloodInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bloodGroup; // "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComponentType componentType = ComponentType.WHOLE_BLOOD;

    @Column(nullable = false)
    private Integer unitsAvailable = 0;

    private Integer reservedUnits = 0;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private String storageLocation = "Blood Bank Main Vault - Level 1";
    private LocalDateTime lastRestockedAt = LocalDateTime.now();

    public enum ComponentType {
        WHOLE_BLOOD,
        PRBC,       // Packed Red Blood Cells
        PLATELETS,  // Platelet Concentrate
        FFP         // Fresh Frozen Plasma
    }

    public BloodInventory() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public ComponentType getComponentType() { return componentType; }
    public void setComponentType(ComponentType componentType) { this.componentType = componentType; }

    public Integer getUnitsAvailable() { return unitsAvailable; }
    public void setUnitsAvailable(Integer unitsAvailable) { this.unitsAvailable = unitsAvailable; }

    public Integer getReservedUnits() { return reservedUnits; }
    public void setReservedUnits(Integer reservedUnits) { this.reservedUnits = reservedUnits; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public LocalDateTime getLastRestockedAt() { return lastRestockedAt; }
    public void setLastRestockedAt(LocalDateTime lastRestockedAt) { this.lastRestockedAt = lastRestockedAt; }
}
