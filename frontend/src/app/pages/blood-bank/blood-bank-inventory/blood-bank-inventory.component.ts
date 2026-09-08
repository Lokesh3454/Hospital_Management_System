import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BloodBankService } from '../../../core/services/blood-bank.service';
import { AuthService } from '../../../core/services/auth.service';
import { BloodInventory } from '../../../models/blood-bank.model';

@Component({
  selector: 'app-blood-bank-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './blood-bank-inventory.component.html',
  styleUrls: ['./blood-bank-inventory.component.css']
})
export class BloodBankComponent implements OnInit {
  inventory: BloodInventory[] = [];
  alerts: BloodInventory[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  selectedGroupFilter: string = 'ALL';
  selectedComponentFilter: string = 'ALL';

  // Reserve Modal
  selectedItemForAction: BloodInventory | null = null;
  showReserveModal = false;
  reserveUnitsCount = 1;

  // Restock Modal
  showRestockModal = false;
  restockUnitsCount = 5;

  // Add New Inventory Record Modal
  showAddModal = false;
  newInventory: BloodInventory = {
    bloodGroup: 'O+',
    componentType: 'WHOLE_BLOOD',
    unitsAvailable: 15,
    reservedUnits: 0,
    expiryDate: new Date(Date.now() + 35 * 86400000).toISOString().split('T')[0],
    storageLocation: 'Cryo-Vault Bay B'
  };

  bloodGroups = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
  componentTypes: ('WHOLE_BLOOD' | 'PRBC' | 'PLATELETS' | 'FFP')[] = ['WHOLE_BLOOD', 'PRBC', 'PLATELETS', 'FFP'];

  constructor(
    public bloodBankService: BloodBankService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.bloodBankService.getAll().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.inventory = res.data;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load blood bank inventory.';
        this.isLoading = false;
      }
    });

    this.bloodBankService.getAlerts().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.alerts = res.data;
        }
      }
    });
  }

  get filteredInventory(): BloodInventory[] {
    return this.inventory.filter(item => {
      const matchGroup = this.selectedGroupFilter === 'ALL' || item.bloodGroup === this.selectedGroupFilter;
      const matchComp = this.selectedComponentFilter === 'ALL' || item.componentType === this.selectedComponentFilter;
      return matchGroup && matchComp;
    });
  }

  get totalAvailableUnits(): number {
    return this.inventory.reduce((sum, item) => sum + (item.unitsAvailable || 0), 0);
  }

  get totalReservedUnits(): number {
    return this.inventory.reduce((sum, item) => sum + (item.reservedUnits || 0), 0);
  }

  get criticalShortagesCount(): number {
    return this.inventory.filter(item => item.unitsAvailable < 5).length;
  }

  openReserveModal(item: BloodInventory): void {
    this.selectedItemForAction = item;
    this.reserveUnitsCount = 1;
    this.showReserveModal = true;
  }

  closeReserveModal(): void {
    this.showReserveModal = false;
    this.selectedItemForAction = null;
  }

  confirmReserve(): void {
    if (!this.selectedItemForAction || !this.selectedItemForAction.id) return;
    if (this.reserveUnitsCount <= 0 || this.reserveUnitsCount > this.selectedItemForAction.unitsAvailable) {
      alert('Invalid unit count. Must be between 1 and available quantity.');
      return;
    }
    this.bloodBankService.reserveUnits(this.selectedItemForAction.id, this.reserveUnitsCount).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.inventory.findIndex(x => x.id === this.selectedItemForAction!.id);
          if (idx !== -1) this.inventory[idx] = res.data;
          this.successMessage = `Reserved ${this.reserveUnitsCount} units of ${res.data.bloodGroup} (${res.data.componentType})`;
          setTimeout(() => this.successMessage = null, 3500);
        }
        this.closeReserveModal();
      },
      error: (err) => {
        alert('Failed to reserve units: ' + (err.error?.message || err.message));
      }
    });
  }

  openRestockModal(item: BloodInventory): void {
    this.selectedItemForAction = item;
    this.restockUnitsCount = 5;
    this.showRestockModal = true;
  }

  closeRestockModal(): void {
    this.showRestockModal = false;
    this.selectedItemForAction = null;
  }

  confirmRestock(): void {
    if (!this.selectedItemForAction || !this.selectedItemForAction.id) return;
    if (this.restockUnitsCount <= 0) {
      alert('Please enter a positive number of units.');
      return;
    }
    this.bloodBankService.restockUnits(this.selectedItemForAction.id, this.restockUnitsCount).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const idx = this.inventory.findIndex(x => x.id === this.selectedItemForAction!.id);
          if (idx !== -1) this.inventory[idx] = res.data;
          this.successMessage = `Successfully restocked +${this.restockUnitsCount} units of ${res.data.bloodGroup}!`;
          setTimeout(() => this.successMessage = null, 3500);
        }
        this.closeRestockModal();
      },
      error: (err) => {
        alert('Failed to restock units: ' + (err.error?.message || err.message));
      }
    });
  }

  openAddModal(): void {
    this.newInventory = {
      bloodGroup: 'O+',
      componentType: 'WHOLE_BLOOD',
      unitsAvailable: 10,
      reservedUnits: 0,
      expiryDate: new Date(Date.now() + 35 * 86400000).toISOString().split('T')[0],
      storageLocation: 'Cryo-Vault Bay B'
    };
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
  }

  saveInventory(): void {
    this.bloodBankService.saveInventory(this.newInventory).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.inventory.unshift(res.data);
          this.successMessage = `Added ${res.data.bloodGroup} inventory record.`;
          setTimeout(() => this.successMessage = null, 3500);
          this.closeAddModal();
        }
      },
      error: (err) => {
        alert('Failed to add inventory: ' + (err.error?.message || err.message));
      }
    });
  }
}
