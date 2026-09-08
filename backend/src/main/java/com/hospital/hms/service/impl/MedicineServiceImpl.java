package com.hospital.hms.service.impl;

import com.hospital.hms.dto.MedicineDTO;
import com.hospital.hms.entity.Medicine;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.MedicineRepository;
import com.hospital.hms.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    @Autowired
    public MedicineServiceImpl(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineDTO> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(MedicineDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineDTO> searchMedicines(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllMedicines();
        }
        return medicineRepository.findByNameContainingIgnoreCaseOrGenericNameContainingIgnoreCase(query.trim(), query.trim())
                .stream()
                .map(MedicineDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineDTO> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines()
                .stream()
                .map(MedicineDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineDTO getMedicineById(Long id) {
        Medicine m = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
        return MedicineDTO.fromEntity(m);
    }

    @Override
    @Transactional
    public MedicineDTO createMedicine(MedicineDTO dto) {
        Medicine m = new Medicine();
        m.setName(dto.getName());
        m.setGenericName(dto.getGenericName());
        m.setCategory(dto.getCategory());
        m.setDosageForm(dto.getDosageForm());
        m.setStrength(dto.getStrength());
        m.setManufacturer(dto.getManufacturer());
        m.setBatchNumber(dto.getBatchNumber());
        m.setUnitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : 0.0);
        m.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 0);
        m.setReorderLevel(dto.getReorderLevel() != null ? dto.getReorderLevel() : 10);
        m.setExpiryDate(dto.getExpiryDate());

        Medicine saved = medicineRepository.save(m);
        return MedicineDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public MedicineDTO updateMedicine(Long id, MedicineDTO dto) {
        Medicine m = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));

        if (dto.getName() != null) m.setName(dto.getName());
        if (dto.getGenericName() != null) m.setGenericName(dto.getGenericName());
        if (dto.getCategory() != null) m.setCategory(dto.getCategory());
        if (dto.getDosageForm() != null) m.setDosageForm(dto.getDosageForm());
        if (dto.getStrength() != null) m.setStrength(dto.getStrength());
        if (dto.getManufacturer() != null) m.setManufacturer(dto.getManufacturer());
        if (dto.getBatchNumber() != null) m.setBatchNumber(dto.getBatchNumber());
        if (dto.getUnitPrice() != null) m.setUnitPrice(dto.getUnitPrice());
        if (dto.getStockQuantity() != null) m.setStockQuantity(dto.getStockQuantity());
        if (dto.getReorderLevel() != null) m.setReorderLevel(dto.getReorderLevel());
        if (dto.getExpiryDate() != null) m.setExpiryDate(dto.getExpiryDate());

        Medicine saved = medicineRepository.save(m);
        return MedicineDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public MedicineDTO adjustStock(Long id, Integer quantityDelta) {
        Medicine m = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));

        int newQuantity = (m.getStockQuantity() != null ? m.getStockQuantity() : 0) + quantityDelta;
        if (newQuantity < 0) {
            throw new BadRequestException("Cannot reduce stock below zero. Current stock: " + m.getStockQuantity());
        }
        m.setStockQuantity(newQuantity);
        Medicine saved = medicineRepository.save(m);
        return MedicineDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteMedicine(Long id) {
        if (!medicineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medicine not found with id: " + id);
        }
        medicineRepository.deleteById(id);
    }
}
