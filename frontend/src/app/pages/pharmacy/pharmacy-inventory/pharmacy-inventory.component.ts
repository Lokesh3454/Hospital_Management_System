import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MedicineService } from '../../../core/services/medicine.service';
import { AuthService } from '../../../core/services/auth.service';
import { Medicine, DosageForm } from '../../../models/medicine.model';

@Component({
  selector: 'app-pharmacy-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './pharmacy-inventory.component.html',
  styleUrls: ['./pharmacy-inventory.component.css']
})
export class PharmacyInventoryComponent implements OnInit {
  medicines: Medicine[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  searchQuery = '';
  filterLowStockOnly = false;
  selectedCategory = 'ALL';

  // Medicine Details Modal (Card/Row Click Feature)
  showMedicineDetailsModal = false;
  selectedMedicineForDetails: Medicine | null = null;
  customAdjustUnits = 10;
  isAdjustingInModal = false;

  // Add Medicine Modal State
  showAddModal = false;
  newMedicine: Medicine = {
    name: '',
    genericName: '',
    category: 'General',
    dosageForm: 'TABLET',
    strength: '',
    manufacturer: '',
    batchNumber: '',
    unitPrice: 5.0,
    stockQuantity: 100,
    reorderLevel: 20
  };
  isSubmitting = false;

  constructor(
    private medicineService: MedicineService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadMedicines();
  }

  loadMedicines(): void {
    this.isLoading = true;
    this.medicineService.getAllMedicines(this.searchQuery).subscribe({
      next: (res) => {
        this.medicines = res.data || [];
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load medicines: ' + (err.error?.message || err.message);
        this.isLoading = false;
      }
    });
  }

  onSearch(): void {
    this.loadMedicines();
  }

  // KPI Card Click Filter
  filterByKpi(type: 'ALL' | 'LOW_STOCK'): void {
    if (type === 'LOW_STOCK') {
      this.filterLowStockOnly = !this.filterLowStockOnly;
    } else {
      this.filterLowStockOnly = false;
      this.selectedCategory = 'ALL';
      this.searchQuery = '';
      this.loadMedicines();
    }
  }

  get filteredMedicines(): Medicine[] {
    return this.medicines.filter(m => {
      const matchLowStock = !this.filterLowStockOnly || (m.stockQuantity <= m.reorderLevel);
      const matchCategory = this.selectedCategory === 'ALL' || m.category === this.selectedCategory;
      return matchLowStock && matchCategory;
    });
  }

  get totalItems(): number { return this.medicines.length; }
  get lowStockCount(): number {
    return this.medicines.filter(m => m.stockQuantity <= m.reorderLevel).length;
  }
  get totalUnits(): number {
    return this.medicines.reduce((acc, m) => acc + (m.stockQuantity || 0), 0);
  }

  categories(): string[] {
    const set = new Set<string>();
    this.medicines.forEach(m => {
      if (m.category) set.add(m.category);
    });
    return Array.from(set);
  }

  // ==========================================
  // CARD / ROW CLICK FEATURE: Medicine Details Modal
  // ==========================================
  openMedicineDetails(med: Medicine): void {
    this.selectedMedicineForDetails = med;
    this.showMedicineDetailsModal = true;
    this.customAdjustUnits = 10;
  }

  closeMedicineDetailsModal(): void {
    this.showMedicineDetailsModal = false;
    this.selectedMedicineForDetails = null;
  }

  adjustStock(med: Medicine, delta: number): void {
    if (!med.id) return;
    this.medicineService.adjustStock(med.id, delta).subscribe({
      next: (res) => {
        if (res.data) {
          med.stockQuantity = res.data.stockQuantity;
          med.isLowStock = res.data.isLowStock;
        }
        this.successMessage = `Updated stock for ${med.name} (${delta > 0 ? '+' : ''}${delta} units)`;
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.errorMessage = 'Stock adjustment failed: ' + (err.error?.message || err.message);
        setTimeout(() => this.errorMessage = null, 4000);
      }
    });
  }

  adjustStockInModal(delta: number): void {
    if (!this.selectedMedicineForDetails || !this.selectedMedicineForDetails.id) return;
    this.isAdjustingInModal = true;
    this.medicineService.adjustStock(this.selectedMedicineForDetails.id, delta).subscribe({
      next: (res) => {
        this.isAdjustingInModal = false;
        if (res.data && this.selectedMedicineForDetails) {
          this.selectedMedicineForDetails.stockQuantity = res.data.stockQuantity;
          this.selectedMedicineForDetails.isLowStock = res.data.isLowStock;
        }
        this.successMessage = `Stock adjusted for ${this.selectedMedicineForDetails?.name} by ${delta > 0 ? '+' : ''}${delta} units.`;
        setTimeout(() => this.successMessage = null, 3500);
      },
      error: (err) => {
        this.isAdjustingInModal = false;
        this.errorMessage = 'Stock adjustment failed: ' + (err.error?.message || err.message);
        setTimeout(() => this.errorMessage = null, 4000);
      }
    });
  }

  openAddModal(): void {
    this.newMedicine = {
      name: '',
      genericName: '',
      category: 'General',
      dosageForm: 'TABLET',
      strength: '',
      manufacturer: '',
      batchNumber: 'BATCH-' + Math.floor(100 + Math.random() * 900),
      unitPrice: 5.0,
      stockQuantity: 100,
      reorderLevel: 20
    };
    this.showAddModal = true;
  }

  closeAddModal(): void {
    this.showAddModal = false;
  }

  saveMedicine(): void {
    if (!this.newMedicine.name) return;
    this.isSubmitting = true;
    this.medicineService.createMedicine(this.newMedicine).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.closeAddModal();
        this.successMessage = `Added ${this.newMedicine.name} to pharmacy inventory.`;
        this.loadMedicines();
        setTimeout(() => this.successMessage = null, 4000);
      },
      error: (err) => {
        this.errorMessage = 'Failed to add medicine: ' + (err.error?.message || err.message);
        this.isSubmitting = false;
      }
    });
  }
}
