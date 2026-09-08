import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AppointmentService } from '../../../core/services/appointment.service';
import { PatientService } from '../../../core/services/patient.service';
import { AuthService } from '../../../core/services/auth.service';
import { Appointment } from '../../../models/appointment.model';
import { Patient } from '../../../models/patient.model';

interface ChatMessage {
  sender: string;
  isMe: boolean;
  text: string;
  time: string;
}

@Component({
  selector: 'app-telemedicine-room',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './telemedicine-room.component.html',
  styleUrls: ['./telemedicine-room.component.css']
})
export class TelemedicineRoomComponent implements OnInit, OnDestroy {
  @ViewChild('localVideo') localVideoRef!: ElementRef<HTMLVideoElement>;

  appointmentId!: number;
  appointment: Appointment | null = null;
  patient: Patient | null = null;
  isLoading = false;

  // Media Controls
  isAudioMuted = false;
  isVideoOff = false;
  isScreenSharing = false;
  localStream: MediaStream | null = null;
  cameraPermitted = false;

  // In-call Chat
  chatMessages: ChatMessage[] = [];
  newMessageText = '';

  callDurationSeconds = 0;
  timerInterval: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private appointmentService: AppointmentService,
    private patientService: PatientService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('appointmentId');
    if (idParam) {
      this.appointmentId = Number(idParam);
      this.loadAppointment();
    } else {
      this.router.navigate(['/appointments']);
    }

    this.timerInterval = setInterval(() => {
      this.callDurationSeconds++;
    }, 1000);

    // Initial greeting message
    const now = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    this.chatMessages.push({
      sender: 'Clinical Telemedicine Bot',
      isMe: false,
      text: 'Encrypted clinical consultation session initialized. Audio and video channels are secure.',
      time: now
    });

    this.startCamera();
  }

  ngOnDestroy(): void {
    if (this.timerInterval) clearInterval(this.timerInterval);
    this.stopCamera();
  }

  get formattedDuration(): string {
    const mins = Math.floor(this.callDurationSeconds / 60);
    const secs = this.callDurationSeconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  loadAppointment(): void {
    this.isLoading = true;
    this.appointmentService.getById(this.appointmentId).subscribe({
      next: (res) => {
        this.appointment = res.data || null;
        this.isLoading = false;
        if (this.appointment && this.appointment.patientId) {
          this.loadPatient(this.appointment.patientId);
        }
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  loadPatient(patientId: number): void {
    this.patientService.getById(patientId).subscribe({
      next: (res) => {
        this.patient = res.data || null;
      },
      error: () => {}
    });
  }

  startCamera(): void {
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      navigator.mediaDevices.getUserMedia({ video: true, audio: true })
        .then((stream) => {
          this.localStream = stream;
          this.cameraPermitted = true;
          if (this.localVideoRef && this.localVideoRef.nativeElement) {
            this.localVideoRef.nativeElement.srcObject = stream;
          }
        })
        .catch(() => {
          // Camera denied or no camera device attached; graceful simulation will run
          this.cameraPermitted = false;
        });
    }
  }

  stopCamera(): void {
    if (this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop());
      this.localStream = null;
    }
  }

  toggleAudio(): void {
    this.isAudioMuted = !this.isAudioMuted;
    if (this.localStream) {
      this.localStream.getAudioTracks().forEach(t => t.enabled = !this.isAudioMuted);
    }
  }

  toggleVideo(): void {
    this.isVideoOff = !this.isVideoOff;
    if (this.localStream) {
      this.localStream.getVideoTracks().forEach(t => t.enabled = !this.isVideoOff);
    }
  }

  toggleScreenShare(): void {
    this.isScreenSharing = !this.isScreenSharing;
  }

  sendMessage(): void {
    if (!this.newMessageText.trim()) return;
    const now = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    this.chatMessages.push({
      sender: this.authService.currentUser?.username || 'You',
      isMe: true,
      text: this.newMessageText.trim(),
      time: now
    });
    this.newMessageText = '';
  }

  endCall(): void {
    this.stopCamera();
    this.router.navigate(['/appointments', this.appointmentId]);
  }
}
