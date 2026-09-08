package com.hospital.hms.service.impl;

import com.hospital.hms.entity.BloodInventory;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.BloodInventoryRepository;
import com.hospital.hms.service.BloodBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BloodBankServiceImpl implements BloodBankService {

    @Autowired
    private BloodInventoryRepository bloodInventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BloodInventory> getAllInventory() {
        return bloodInventoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloodInventory> getInventoryByGroup(String bloodGroup) {
        return bloodInventoryRepository.findByBloodGroup(bloodGroup);
    }

    @Override
    public BloodInventory restockUnits(String bloodGroup, BloodInventory.ComponentType componentType, int units) {
        BloodInventory item = bloodInventoryRepository.findByBloodGroupAndComponentType(bloodGroup, componentType)
                .orElseGet(() -> {
                    BloodInventory bi = new BloodInventory();
                    bi.setBloodGroup(bloodGroup);
                    bi.setComponentType(componentType);
                    bi.setUnitsAvailable(0);
                    bi.setReservedUnits(0);
                    bi.setExpiryDate(LocalDate.now().plusDays(35)); // 35 days standard shelf life
                    return bi;
                });

        item.setUnitsAvailable(item.getUnitsAvailable() + units);
        item.setLastRestockedAt(LocalDateTime.now());
        return bloodInventoryRepository.save(item);
    }

    @Override
    public BloodInventory restockUnits(Long inventoryId, int units) {
        BloodInventory item = bloodInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood inventory not found with id " + inventoryId));
        item.setUnitsAvailable(item.getUnitsAvailable() + units);
        item.setLastRestockedAt(LocalDateTime.now());
        return bloodInventoryRepository.save(item);
    }

    @Override
    public BloodInventory reserveUnits(Long inventoryId, int units) {
        BloodInventory item = bloodInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood inventory not found with id " + inventoryId));

        if (item.getUnitsAvailable() < units) {
            throw new BadRequestException("Insufficient available units for reservation. Available: " + item.getUnitsAvailable());
        }

        item.setUnitsAvailable(item.getUnitsAvailable() - units);
        item.setReservedUnits(item.getReservedUnits() + units);
        return bloodInventoryRepository.save(item);
    }

    @Override
    public BloodInventory issueUnits(Long inventoryId, int units) {
        BloodInventory item = bloodInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood inventory not found with id " + inventoryId));

        if (item.getReservedUnits() >= units) {
            item.setReservedUnits(item.getReservedUnits() - units);
        } else if (item.getUnitsAvailable() >= units) {
            item.setUnitsAvailable(item.getUnitsAvailable() - units);
        } else {
            throw new BadRequestException("Cannot issue " + units + " units. Insufficient inventory.");
        }
        return bloodInventoryRepository.save(item);
    }
}
