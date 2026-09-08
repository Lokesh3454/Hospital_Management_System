export interface NotificationLog {
  id?: number;
  recipientName: string;
  recipientContact: string;
  channel: 'SMS' | 'WHATSAPP' | 'EMAIL' | 'BROADCAST_ALARM';
  triggerEvent: string;
  subject?: string;
  message: string;
  status: 'SENT' | 'DELIVERED' | 'FAILED';
  sentAt?: string;
}
