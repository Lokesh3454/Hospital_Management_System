import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NotificationService } from '../../../core/services/notification.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationLog } from '../../../models/notification.model';

@Component({
  selector: 'app-notification-log',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './notification-log.component.html',
  styleUrls: ['./notification-log.component.css']
})
export class NotificationLogComponent implements OnInit {
  logs: NotificationLog[] = [];
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  selectedChannel: string = 'ALL';
  selectedStatus: string = 'ALL';

  // Test Dispatch Modal
  showDispatchModal = false;
  isDispatching = false;
  newNotification: NotificationLog = {
    recipientName: '',
    recipientContact: '+1-555-0199',
    channel: 'SMS',
    triggerEvent: 'APPOINTMENT_REMINDER',
    subject: 'Hospital Appointment Reminder',
    message: 'Your appointment is confirmed for tomorrow at 10:00 AM. Please arrive 15 minutes early.',
    status: 'DELIVERED'
  };

  constructor(
    public notificationService: NotificationService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.notificationService.getAll().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.logs = res.data;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load notification logs.';
        this.isLoading = false;
      }
    });
  }

  get filteredLogs(): NotificationLog[] {
    return this.logs.filter(l => {
      const matchChannel = this.selectedChannel === 'ALL' || l.channel === this.selectedChannel;
      const matchStatus = this.selectedStatus === 'ALL' || l.status === this.selectedStatus;
      return matchChannel && matchStatus;
    });
  }

  get totalDelivered(): number {
    return this.logs.filter(l => l.status === 'DELIVERED').length;
  }

  get deliveryRate(): number {
    if (this.logs.length === 0) return 100;
    return Math.round((this.totalDelivered / this.logs.length) * 100);
  }

  get smsCount(): number {
    return this.logs.filter(l => l.channel === 'SMS').length;
  }

  get whatsappCount(): number {
    return this.logs.filter(l => l.channel === 'WHATSAPP').length;
  }

  openDispatchModal(): void {
    this.newNotification = {
      recipientName: '',
      recipientContact: '+1-555-0199',
      channel: 'SMS',
      triggerEvent: 'APPOINTMENT_REMINDER',
      subject: 'Hospital Appointment Reminder',
      message: 'Your appointment is confirmed for tomorrow at 10:00 AM. Please bring your insurance card.',
      status: 'DELIVERED'
    };
    this.showDispatchModal = true;
  }

  closeDispatchModal(): void {
    this.showDispatchModal = false;
  }

  onChannelChange(): void {
    if (this.newNotification.channel === 'EMAIL') {
      this.newNotification.recipientContact = 'patient@example.com';
    } else {
      this.newNotification.recipientContact = '+1-555-0199';
    }
  }

  dispatch(): void {
    if (!this.newNotification.recipientName || !this.newNotification.recipientContact || !this.newNotification.message) {
      alert('Please fill in recipient name, contact, and message.');
      return;
    }
    this.isDispatching = true;
    this.notificationService.dispatchNotification(this.newNotification).subscribe({
      next: (res) => {
        this.isDispatching = false;
        if (res.success && res.data) {
          this.logs.unshift(res.data);
          this.successMessage = `Alert dispatched via ${res.data.channel} to ${res.data.recipientName}!`;
          setTimeout(() => this.successMessage = null, 3500);
          this.closeDispatchModal();
        }
      },
      error: (err) => {
        this.isDispatching = false;
        alert('Failed to dispatch notification: ' + (err.error?.message || err.message));
      }
    });
  }

  getChannelBadgeClass(channel: string): string {
    switch (channel) {
      case 'SMS': return 'badge bg-primary';
      case 'WHATSAPP': return 'badge bg-success';
      case 'EMAIL': return 'badge bg-info text-dark';
      case 'BROADCAST_ALARM': return 'badge bg-danger';
      default: return 'badge bg-secondary';
    }
  }

  getChannelIcon(channel: string): string {
    switch (channel) {
      case 'SMS': return 'bi-chat-dots-fill';
      case 'WHATSAPP': return 'bi-whatsapp';
      case 'EMAIL': return 'bi-envelope-fill';
      case 'BROADCAST_ALARM': return 'bi-megaphone-fill';
      default: return 'bi-bell-fill';
    }
  }
}
