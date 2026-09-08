package com.hospital.hms.service;

import com.hospital.hms.entity.BloodInventory;
import java.util.List;

public interface BloodBankService {
    List<BloodInventory> getAllInventory();
    List<BloodInventory> getInventoryByGroup(String bloodGroup);
    BloodInventory restockUnits(String bloodGroup, BloodInventory.ComponentType componentType, int units);
    BloodInventory restockUnits(Long inventoryId, int units);
    BloodInventory reserveUnits(Long inventoryId, int units);
    BloodInventory issueUnits(Long inventoryId, int units);
}
