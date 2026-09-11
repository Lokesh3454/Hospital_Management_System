package com.hospital.hms.service.impl;

import com.hospital.hms.dto.AppointmentRequestDTO;
import com.hospital.hms.dto.AppointmentResponseDTO;
import com.hospital.hms.dto.RescheduleRequestDTO;
import com.hospital.hms.dto.TimeSlotDTO;
import com.hospital.hms.entity.*;
import com.hospital.hms.exception.BadRequestException;
import com.hospital.hms.exception.ResourceNotFoundException;
import com.hospital.hms.repository.AppointmentRepository;
import com.hospital.hms.repository.DoctorAvailabilityRepository;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Override
    public AppointmentResponseDTO bookAppointment(AppointmentRequestDTO request, Long authenticatedUserId) {
        // 1. Patient verification
        Patient patient = null;
        if (request.getPatientId() != null) {
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + request.getPatientId()));
        } else if (authenticatedUserId != null) {
            patient = patientRepository.findByUserId(authenticatedUserId)
                    .orElseThrow(() -> new BadRequestException("No patient profile found for the logged-in user. Please specify patientId."));
        } else {
            throw new BadRequestException("Patient ID is required.");
        }

        // 2. Doctor verification
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + request.getDoctorId()));

        if (doctor.getStatus() == DoctorStatus.INACTIVE) {
            throw new BadRequestException("Doctor is currently inactive and cannot accept new appointments.");
        }

        // 3. Verify Doctor availability on the requested day & time
        validateDoctorAvailability(doctor.getId(), request.getAppointmentDate(), request.getAppointmentTime());

        // 4. Double booking check (same doctor cannot have 2 active appointments at the exact same date and time)
        boolean alreadyBooked = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                doctor.getId(),
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                AppointmentStatus.CANCELLED
        );
        if (alreadyBooked) {
            throw new BadRequestException("The selected time slot (" + request.getAppointmentDate() + " at " +
                    request.getAppointmentTime() + ") is already booked for " +
                    (doctor.getUser() != null ? "Dr. " + doctor.getUser().getLastName() : "this doctor") +
                    ". Please choose an available time slot.");
        }

        // 5. Create appointment
        Appointment appointment = new Appointment(
                patient,
                doctor,
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                AppointmentStatus.BOOKED,
                request.getReason(),
                request.getNotes()
        );

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));
        return AppointmentResponseDTO.fromEntity(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Appointment::getAppointmentDate).reversed()
                        .thenComparing(Appointment::getAppointmentTime).reversed())
                .map(AppointmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getPatientAppointments(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id " + patientId);
        }
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patientId).stream()
                .map(AppointmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getDoctorAppointments(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not found with id " + doctorId);
        }
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(doctorId).stream()
                .map(AppointmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponseDTO rescheduleAppointment(Long id, RescheduleRequestDTO request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot reschedule an appointment that has been cancelled.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot reschedule an appointment that is already completed.");
        }

        // Validate doctor availability on new date & time
        validateDoctorAvailability(appointment.getDoctor().getId(), request.getAppointmentDate(), request.getAppointmentTime());

        // Check conflicts excluding this appointment
        boolean conflict = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotAndIdNot(
                appointment.getDoctor().getId(),
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                AppointmentStatus.CANCELLED,
                appointment.getId()
        );
        if (conflict) {
            throw new BadRequestException("The requested new slot (" + request.getAppointmentDate() + " at " +
                    request.getAppointmentTime() + ") is already booked by another patient.");
        }

        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        if (request.getReason() != null && !request.getReason().isBlank()) {
            appointment.setReason(request.getReason());
        }

        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            String existingNotes = appointment.getNotes() != null ? appointment.getNotes() + "\n" : "";
            appointment.setNotes(existingNotes + "[Rescheduled]: " + request.getNotes());
        }

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponseDTO.fromEntity(saved);
    }

    @Override
    public AppointmentResponseDTO cancelAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel an appointment that has already been completed.");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Appointment is already cancelled.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (reason != null && !reason.isBlank()) {
            String existingNotes = appointment.getNotes() != null ? appointment.getNotes() + "\n" : "";
            appointment.setNotes(existingNotes + "[Cancellation Reason]: " + reason);
        }

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponseDTO.fromEntity(saved);
    }

    @Override
    public AppointmentResponseDTO confirmAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot confirm a cancelled appointment.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Appointment is already marked completed.");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponseDTO.fromEntity(saved);
    }

    @Override
    public AppointmentResponseDTO completeAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot mark a cancelled appointment as completed.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> searchAndFilterAppointments(String search, AppointmentStatus status, LocalDate date, Long doctorId, Long patientId, Long authenticatedUserId) {
        Long scopedPatientId = patientId;
        if (authenticatedUserId != null) {
            Optional<Patient> loggedInPatient = patientRepository.findByUserId(authenticatedUserId);
            if (loggedInPatient.isPresent()) {
                scopedPatientId = loggedInPatient.get().getId();
            }
        }
        return appointmentRepository.searchAndFilter(search, status, date, doctorId, scopedPatientId).stream()
                .map(AppointmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> searchAndFilterAppointments(String search, AppointmentStatus status, LocalDate date, Long doctorId, Long patientId) {
        return searchAndFilterAppointments(search, status, date, doctorId, patientId, null);
    }

    @Override
    @Transactional
    public List<TimeSlotDTO> getAvailableTimeSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));

        String dayOfWeek = date.getDayOfWeek().name();
        List<DoctorAvailability> availabilities = doctorAvailabilityRepository.findByDoctorId(doctorId);

        if (availabilities.isEmpty()) {
            String[] defaultDays = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
            for (String day : defaultDays) {
                LocalTime dStart = LocalTime.of(9, 0);
                LocalTime dEnd = day.equals("SATURDAY") ? LocalTime.of(14, 0) : LocalTime.of(17, 0);
                DoctorAvailability da = new DoctorAvailability(doctor, day, dStart, dEnd, true);
                doctorAvailabilityRepository.save(da);
            }
            availabilities = doctorAvailabilityRepository.findByDoctorId(doctorId);
        }

        DoctorAvailability matching = null;
        for (DoctorAvailability a : availabilities) {
            if (a.getDayOfWeek().equalsIgnoreCase(dayOfWeek)) {
                matching = a;
                break;
            }
        }

        LocalTime start;
        LocalTime end;
        boolean dayAvailable;

        if (matching != null) {
            start = matching.getStartTime();
            end = matching.getEndTime();
            dayAvailable = matching.isAvailable();
        } else if (doctor.getAvailableDays() != null && (
                doctor.getAvailableDays().toUpperCase().contains(dayOfWeek.substring(0, 3)) ||
                doctor.getAvailableDays().toUpperCase().contains(dayOfWeek))) {
            start = LocalTime.of(9, 0);
            end = dayOfWeek.equals("SATURDAY") ? LocalTime.of(14, 0) : LocalTime.of(17, 0);
            dayAvailable = true;
        } else if (!dayOfWeek.equals("SUNDAY")) {
            start = LocalTime.of(9, 0);
            end = dayOfWeek.equals("SATURDAY") ? LocalTime.of(14, 0) : LocalTime.of(17, 0);
            dayAvailable = true;
        } else {
            return Collections.emptyList();
        }

        if (!dayAvailable) {
            return Collections.emptyList();
        }

        // Fetch non-cancelled appointments for this doctor on this date
        List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusNot(
                doctorId, date, AppointmentStatus.CANCELLED
        );
        Set<LocalTime> bookedTimes = bookedAppointments.stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        List<TimeSlotDTO> slots = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        LocalTime current = start;

        while (current.isBefore(end)) {
            boolean isBooked = bookedTimes.contains(current);
            slots.add(new TimeSlotDTO(current, current.format(formatter), !isBooked, isBooked));
            current = current.plusMinutes(30);
        }

        return slots;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDoctorAvailable(Long doctorId, LocalDate date, LocalTime time) {
        try {
            validateDoctorAvailability(doctorId, date, time);
            return !appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                    doctorId, date, time, AppointmentStatus.CANCELLED);
        } catch (Exception e) {
            return false;
        }
    }

    private void validateDoctorAvailability(Long doctorId, LocalDate date, LocalTime time) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id " + doctorId));

        if (doctor.getStatus() == DoctorStatus.INACTIVE) {
            throw new BadRequestException("Doctor is currently inactive.");
        }

        String dayOfWeek = date.getDayOfWeek().name();
        List<DoctorAvailability> availabilities = doctorAvailabilityRepository.findByDoctorId(doctorId);

        DoctorAvailability matching = null;
        for (DoctorAvailability a : availabilities) {
            if (a.getDayOfWeek().equalsIgnoreCase(dayOfWeek)) {
                matching = a;
                break;
            }
        }

        LocalTime start;
        LocalTime end;

        if (matching != null) {
            if (!matching.isAvailable()) {
                throw new BadRequestException("Doctor is marked unavailable on " + dayOfWeek);
            }
            start = matching.getStartTime();
            end = matching.getEndTime();
        } else if (doctor.getAvailableDays() != null && (
                doctor.getAvailableDays().toUpperCase().contains(dayOfWeek.substring(0, 3)) ||
                doctor.getAvailableDays().toUpperCase().contains(dayOfWeek))) {
            start = LocalTime.of(9, 0);
            end = dayOfWeek.equals("SATURDAY") ? LocalTime.of(14, 0) : LocalTime.of(17, 0);
        } else if (!dayOfWeek.equals("SUNDAY")) {
            start = LocalTime.of(9, 0);
            end = dayOfWeek.equals("SATURDAY") ? LocalTime.of(14, 0) : LocalTime.of(17, 0);
        } else {
            throw new BadRequestException("Doctor is not scheduled to work on " + dayOfWeek);
        }

        if (time.isBefore(start) || time.isAfter(end.minusMinutes(1))) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
            throw new BadRequestException("Requested time " + time.format(fmt) + " is outside doctor's working hours (" +
                    start.format(fmt) + " - " + end.format(fmt) + ")");
        }
    }

    @Override
    @Transactional
    public void seedDiverseSampleAppointments() {
        LocalDate today = LocalDate.now();

        // 1. Delete redundant cancelled / orphan test appointments with id > 1
        List<Appointment> existing = appointmentRepository.findAll();
        for (Appointment a : existing) {
            if (a.getId() != null && a.getId() > 1) {
                try {
                    appointmentRepository.delete(a);
                } catch (Exception ignored) {}
            }
        }

        // 2. Fetch doctors and patients
        List<Doctor> allDocs = doctorRepository.findAll();
        List<Patient> allPatients = patientRepository.findAll();
        if (allDocs.isEmpty() || allPatients.isEmpty()) return;

        Doctor drSmith = findDoc(allDocs, "Smith", "Cardiology");
        Doctor drWatson = findDoc(allDocs, "Watson", "Neurology");
        Doctor drVance = findDoc(allDocs, "Vance", "Orthopedics");
        Doctor drKhan = findDoc(allDocs, "Khan", "Pulmonology");
        Doctor drPatel = findDoc(allDocs, "Patel", "Endocrinology");
        Doctor drWilson = findDoc(allDocs, "Wilson", "Gastroenterology");

        Patient patJohn = findPat(allPatients, "John", 0);
        Patient patPriya = findPat(allPatients, "Priya", 1);
        Patient patCarlos = findPat(allPatients, "Carlos", 2);
        Patient patMichael = findPat(allPatients, "Michael", 3);
        Patient patAlice = findPat(allPatients, "Alice", 4);
        Patient patRobert = findPat(allPatients, "Robert", 5);
        Patient patClara = findPat(allPatients, "Clara", 6);
        Patient patSarah = findPat(allPatients, "Sarah", 7);

        // Ensure appointment #1 is active today
        Optional<Appointment> appt1Opt = appointmentRepository.findById(1L);
        if (appt1Opt.isPresent()) {
            Appointment appt1 = appt1Opt.get();
            appt1.setAppointmentDate(today);
            appt1.setAppointmentTime(LocalTime.of(10, 30));
            appt1.setStatus(AppointmentStatus.CONFIRMED);
            appt1.setDoctor(drSmith);
            appt1.setPatient(patJohn);
            appt1.setReason("Routine cardiac checkup and blood pressure monitoring");
            appointmentRepository.save(appt1);
        }

        // Today second appointment: Dr. Watson with Clara
        createAppt(drWatson, patClara, today, LocalTime.of(14, 30), AppointmentStatus.CONFIRMED, "Migraine headache evaluation and trigger mapping");

        // Upcoming Future Appointments:
        // Tomorrow: Dr. Smith with Michael Chang
        createAppt(drSmith, patMichael, today.plusDays(1), LocalTime.of(10, 0), AppointmentStatus.CONFIRMED, "Post-stent cardiac evaluation and lipid review");

        // +2 days: Dr. Watson with Alice Wonder
        createAppt(drWatson, patAlice, today.plusDays(2), LocalTime.of(9, 30), AppointmentStatus.CONFIRMED, "Peripheral neuropathy assessment and nerve conduction review");

        // +3 days: Dr. Smith with John Doe
        createAppt(drSmith, patJohn, today.plusDays(3), LocalTime.of(11, 30), AppointmentStatus.BOOKED, "Follow-up blood pressure titration and stress ECG discussion");

        // +4 days: Dr. Khan with Carlos Mendez
        createAppt(drKhan, patCarlos, today.plusDays(4), LocalTime.of(10, 0), AppointmentStatus.CONFIRMED, "Chronic asthma spirometry follow-up and inhaler review");

        // +5 days: Dr. Vance with Robert Fox
        createAppt(drVance, patRobert, today.plusDays(5), LocalTime.of(14, 0), AppointmentStatus.CONFIRMED, "L4-L5 lumbar disc herniation MRI consultation");

        // +6 days: Dr. Patel with Priya Sharma
        createAppt(drPatel, patPriya, today.plusDays(6), LocalTime.of(11, 0), AppointmentStatus.BOOKED, "Type 2 Diabetes Mellitus glycemic control evaluation");

        // +7 days: Dr. Wilson with Clara Oswald
        createAppt(drWilson, patClara, today.plusDays(7), LocalTime.of(15, 0), AppointmentStatus.CONFIRMED, "GERD reflux follow-up and dietary counseling");

        // Past / History Appointments:
        createAppt(drSmith, patJohn, today.minusDays(14), LocalTime.of(11, 30), AppointmentStatus.COMPLETED, "Initial cardiology consultation and baseline ECG");
        createAppt(drWatson, patSarah, today.minusDays(21), LocalTime.of(10, 0), AppointmentStatus.COMPLETED, "Chronic tension headache diagnostic review");
        createAppt(drPatel, patPriya, today.minusDays(30), LocalTime.of(11, 0), AppointmentStatus.COMPLETED, "Routine annual endocrinology wellness checkup");
        createAppt(drSmith, patJohn, today.minusDays(7), LocalTime.of(14, 0), AppointmentStatus.CANCELLED, "Patient rescheduled due to work travel");
    }

    private void createAppt(Doctor doctor, Patient patient, LocalDate date, LocalTime time, AppointmentStatus status, String reason) {
        Appointment a = new Appointment(patient, doctor, date, time, status, reason);
        appointmentRepository.save(a);
    }

    private Doctor findDoc(List<Doctor> docs, String lastName, String spec) {
        for (Doctor d : docs) {
            if (d.getUser() != null && d.getUser().getLastName() != null && d.getUser().getLastName().equalsIgnoreCase(lastName)) {
                return d;
            }
            if (d.getSpecialization() != null && d.getSpecialization().equalsIgnoreCase(spec)) {
                return d;
            }
        }
        return docs.get(0);
    }

    private Patient findPat(List<Patient> pats, String firstName, int fallbackIndex) {
        for (Patient p : pats) {
            if (p.getUser() != null && p.getUser().getFirstName() != null && p.getUser().getFirstName().equalsIgnoreCase(firstName)) {
                return p;
            }
        }
        return pats.get(fallbackIndex % pats.size());
    }
}
