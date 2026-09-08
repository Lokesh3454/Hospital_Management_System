import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AppointmentService } from '../../../core/services/appointment.service';
import { Appointment } from '../../../models/appointment.model';
import { AuthService } from '../../../core/services/auth.service';

interface DoctorQueue {
  doctorId: number;
  doctorName: string;
  department: string;
  roomNumber: string;
  currentServing: Appointment | null;
  upNext: Appointment[];
}

@Component({
  selector: 'app-queue-display',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './queue-display.component.html',
  styleUrls: ['./queue-display.component.css']
})
export class QueueDisplayComponent implements OnInit, OnDestroy {
  currentTime = new Date();
  timeInterval: any;
  pollInterval: any;
  soundEnabled = true;
  isFullscreen = false;

  doctorQueues: DoctorQueue[] = [];
  todayAppointments: Appointment[] = [];
  isLoading = false;

  constructor(
    private appointmentService: AppointmentService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.timeInterval = setInterval(() => {
      this.currentTime = new Date();
    }, 1000);

    this.loadTodayAppointments();

    // Auto-refresh queue every 15 seconds
    this.pollInterval = setInterval(() => {
      this.loadTodayAppointments(true);
    }, 15000);
  }

  ngOnDestroy(): void {
    if (this.timeInterval) clearInterval(this.timeInterval);
    if (this.pollInterval) clearInterval(this.pollInterval);
  }

  loadTodayAppointments(silent = false): void {
    if (!silent) this.isLoading = true;
    const today = new Date().toISOString().split('T')[0];

    this.appointmentService.getAll({ date: today }).subscribe({
      next: (res) => {
        this.todayAppointments = res.data || [];
        this.organizeQueues();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  organizeQueues(): void {
    const queueMap = new Map<number, DoctorQueue>();

    for (const appt of this.todayAppointments) {
      if (appt.status === 'CANCELLED' || appt.status === 'COMPLETED') continue;

      const docId = appt.doctorId || 0;
      if (!queueMap.has(docId)) {
        queueMap.set(docId, {
          doctorId: docId,
          doctorName: appt.doctorName || 'Attending Physician',
          department: appt.doctorDepartment || 'General Medicine',
          roomNumber: appt.doctorRoomNumber || 'Suite ' + (100 + docId),
          currentServing: null,
          upNext: []
        });
      }

      const q = queueMap.get(docId)!;
      // First confirmed or booked appointment is currently serving
      if (!q.currentServing && (appt.status === 'CONFIRMED' || appt.status === 'BOOKED')) {
        q.currentServing = appt;
      } else {
        q.upNext.push(appt);
      }
    }

    this.doctorQueues = Array.from(queueMap.values());
  }

  callNext(q: DoctorQueue): void {
    if (q.upNext.length > 0) {
      const nextAppt = q.upNext.shift()!;
      q.currentServing = nextAppt;
      this.playChime();
    }
  }

  playChime(): void {
    if (!this.soundEnabled) return;
    try {
      const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
      const now = audioCtx.currentTime;

      // Note 1: E5 (659.25 Hz)
      const osc1 = audioCtx.createOscillator();
      const gain1 = audioCtx.createGain();
      osc1.frequency.setValueAtTime(659.25, now);
      gain1.gain.setValueAtTime(0.3, now);
      gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.5);
      osc1.connect(gain1);
      gain1.connect(audioCtx.destination);
      osc1.start(now);
      osc1.stop(now + 0.5);

      // Note 2: C5 (523.25 Hz) - classic chime
      const osc2 = audioCtx.createOscillator();
      const gain2 = audioCtx.createGain();
      osc2.frequency.setValueAtTime(523.25, now + 0.25);
      gain2.gain.setValueAtTime(0.3, now + 0.25);
      gain2.gain.exponentialRampToValueAtTime(0.001, now + 0.8);
      osc2.connect(gain2);
      gain2.connect(audioCtx.destination);
      osc2.start(now + 0.25);
      osc2.stop(now + 0.8);
    } catch (e) {
      console.warn('Audio synthesis not permitted without interaction', e);
    }
  }

  toggleSound(): void {
    this.soundEnabled = !this.soundEnabled;
    if (this.soundEnabled) {
      this.playChime();
    }
  }

  toggleFullscreen(): void {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen().then(() => {
        this.isFullscreen = true;
      }).catch(() => {});
    } else {
      document.exitFullscreen().then(() => {
        this.isFullscreen = false;
      }).catch(() => {});
    }
  }
}
