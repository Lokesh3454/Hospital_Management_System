import { Component, HostBinding } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { SidebarService } from '../../../core/services/sidebar.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @HostBinding('class.mobile-open') get isMobileOpen(): boolean {
    return this.sidebarService.isMobileOpen();
  }

  constructor(
    public authService: AuthService,
    public sidebarService: SidebarService
  ) {}

  onNavClick(): void {
    if (typeof window !== 'undefined' && window.innerWidth < 992) {
      this.sidebarService.close();
    }
  }

  get isAdmin(): boolean {
    return this.authService.hasRole('ROLE_ADMIN');
  }

  get isDoctor(): boolean {
    return this.authService.hasRole('ROLE_DOCTOR');
  }

  get isPatient(): boolean {
    return this.authService.hasRole('ROLE_PATIENT');
  }

  get isReceptionist(): boolean {
    return this.authService.hasRole('ROLE_RECEPTIONIST');
  }
}
