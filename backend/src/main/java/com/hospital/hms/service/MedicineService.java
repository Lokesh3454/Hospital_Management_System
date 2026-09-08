package com.hospital.hms.service;

import com.hospital.hms.dto.MedicineDTO;
import java.util.List;

public interface MedicineService {
    List<MedicineDTO> getAllMedicines();
    List<MedicineDTO> searchMedicines(String query);
    List<MedicineDTO> getLowStockMedicines();
    MedicineDTO getMedicineById(Long id);
    MedicineDTO createMedicine(MedicineDTO dto);
    MedicineDTO updateMedicine(Long id, MedicineDTO dto);
    MedicineDTO adjustStock(Long id, Integer quantityDelta);
    void deleteMedicine(Long id);
}
