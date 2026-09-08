import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { SidebarService } from '../../../core/services/sidebar.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  constructor(
    public authService: AuthService,
    public sidebarService: SidebarService
  ) {}

  get roleBadgeClass(): string {
    const user = this.authService.currentUserValue;
    if (!user || !user.roles) return 'bg-secondary';
    if (user.roles.includes('ROLE_ADMIN')) return 'bg-danger';
    if (user.roles.includes('ROLE_DOCTOR')) return 'bg-primary';
    if (user.roles.includes('ROLE_RECEPTIONIST')) return 'bg-warning text-dark';
    if (user.roles.includes('ROLE_PATIENT')) return 'bg-success';
    return 'bg-info text-dark';
  }

  get displayRoleName(): string {
    const user = this.authService.currentUserValue;
    if (!user || !user.roles || user.roles.length === 0) return '';
    const role = user.roles[0];
    return role.replace('ROLE_', '');
  }

  logout(): void {
    this.authService.logout();
  }
}
