package com.hospital.hms.dto;

import com.hospital.hms.entity.DosageForm;
import com.hospital.hms.entity.Medicine;
import java.time.LocalDate;

public class MedicineDTO {

    private Long id;
    private String name;
    private String genericName;
    private String category;
    private DosageForm dosageForm;
    private String strength;
    private String manufacturer;
    private String batchNumber;
    private Double unitPrice;
    private Integer stockQuantity;
    private Integer reorderLevel;
    private LocalDate expiryDate;
    private Boolean isLowStock;
    private Boolean isExpired;

    public MedicineDTO() {}

    public static MedicineDTO fromEntity(Medicine m) {
        if (m == null) return null;
        MedicineDTO dto = new MedicineDTO();
        dto.setId(m.getId());
        dto.setName(m.getName());
        dto.setGenericName(m.getGenericName());
        dto.setCategory(m.getCategory());
        dto.setDosageForm(m.getDosageForm());
        dto.setStrength(m.getStrength());
        dto.setManufacturer(m.getManufacturer());
        dto.setBatchNumber(m.getBatchNumber());
        dto.setUnitPrice(m.getUnitPrice());
        dto.setStockQuantity(m.getStockQuantity());
        dto.setReorderLevel(m.getReorderLevel());
        dto.setExpiryDate(m.getExpiryDate());

        dto.setIsLowStock(m.getStockQuantity() != null && m.getReorderLevel() != null && m.getStockQuantity() <= m.getReorderLevel());
        dto.setIsExpired(m.getExpiryDate() != null && m.getExpiryDate().isBefore(LocalDate.now()));

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public DosageForm getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(DosageForm dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Boolean getIsLowStock() {
        return isLowStock;
    }

    public void setIsLowStock(Boolean lowStock) {
        isLowStock = lowStock;
    }

    public Boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(Boolean expired) {
        isExpired = expired;
    }
}
