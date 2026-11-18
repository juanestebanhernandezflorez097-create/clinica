package com.example.clinica.services;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.clinica.entities.AppointmentEntity;
import com.example.clinica.entities.DoctorEntity;
import com.example.clinica.entities.PatientEntity;
import com.example.clinica.exceptions.BusinessRuleException;
import com.example.clinica.exceptions.ConflictException;
import com.example.clinica.exceptions.NotFoundException;
import com.example.clinica.mappers.AppointmentMapper;
import com.example.clinica.models.Appointment;
import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.models.InsuranceType;
import com.example.clinica.repositories.AppointmentRepository;
import com.example.clinica.repositories.DoctorRepository;
import com.example.clinica.repositories.PatientRepository;

@Service
public class AppointmentService {

    private static final LocalTime FIRST_SLOT = LocalTime.of(8, 0);
    private static final LocalTime LAST_SLOT = LocalTime.of(16, 30);
    private static final int MAX_NO_SHOWS = 2;
    private static final Duration MIN_CANCEL_ADVANCE = Duration.ofHours(2);

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final Clock clock;

    public AppointmentService(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository,
            PatientRepository patientRepository, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ agendar

    @Transactional
    public Appointment create(Appointment appointment) {
        String license = appointment.getDoctor().getLicense();
        String document = appointment.getPatient().getDocument();
        LocalDateTime dateTime = appointment.getDateTime();

        DoctorEntity doctor = doctorRepository.findByLicense(license)
                .orElseThrow(() -> new NotFoundException("No existe un médico con licencia " + license));
        PatientEntity patient = patientRepository.findByDocument(document)
                .orElseThrow(() -> new NotFoundException("No existe un paciente con documento " + document));

        validateSlot(dateTime);                                      // RN1, RN2, RN3
        validatePatientCanSchedule(patient);                         // RN7
        validateDoctorAvailable(doctor, dateTime);                   // RN4
        validatePatientHasNoAppointmentThatDay(patient, dateTime);   // RN5

        AppointmentEntity entity = AppointmentMapper.modelToEntity(appointment);
        entity.setNumber(generateNumber());
        entity.setDoctor(doctor);
        entity.setPatient(patient);
        entity.setStatus(AppointmentStatus.SCHEDULED);
        entity.setAmountToPay(calculateAmount(doctor.getConsultationFee(), patient.getInsuranceType())); // RN6

        return AppointmentMapper.entityToModel(appointmentRepository.save(entity));
    }

    // ---------------------------------------------------------------- consultas

    public Appointment getByNumber(String number) {
        return AppointmentMapper.entityToModel(findEntity(number));
    }

    public List<Appointment> getByDoctorAndDate(String doctorLicense, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return appointmentRepository
                .findByDoctor_LicenseAndDateTimeBetweenOrderByDateTime(doctorLicense, start, end.minusNanos(1))
                .stream().map(AppointmentMapper::entityToModel).toList();
    }

    public List<Appointment> getByPatient(String document) {
        return appointmentRepository.findByPatient_DocumentOrderByDateTimeDesc(document)
                .stream().map(AppointmentMapper::entityToModel).toList();
    }

    // ------------------------------------------------------- cambios de estado

    @Transactional
    public Appointment cancel(String number) {
        AppointmentEntity entity = findScheduled(number, "cancelar");
        Duration remaining = Duration.between(LocalDateTime.now(clock), entity.getDateTime());
        if (remaining.compareTo(MIN_CANCEL_ADVANCE) <= 0) { // RN8: debe faltar MÁS de 2 horas
            throw new BusinessRuleException("Sólo se puede cancelar con más de 2 horas de anticipación");
        }
        entity.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentMapper.entityToModel(appointmentRepository.save(entity));
    }

    @Transactional
    public Appointment attend(String number) {
        AppointmentEntity entity = findScheduled(number, "marcar como atendida");
        entity.setStatus(AppointmentStatus.ATTENDED);
        return AppointmentMapper.entityToModel(appointmentRepository.save(entity));
    }

    @Transactional
    public Appointment noShow(String number) {
        AppointmentEntity entity = findScheduled(number, "marcar como inasistencia");
        entity.setStatus(AppointmentStatus.NO_SHOW);

        PatientEntity patient = entity.getPatient();
        patient.setNoShowCount(patient.getNoShowCount() + 1);
        patientRepository.save(patient);

        return AppointmentMapper.entityToModel(appointmentRepository.save(entity));
    }

    // ------------------------------------------------------- reglas (privadas)

    private void validateSlot(LocalDateTime dateTime) {
        // RN1: horas en punto o y media
        if ((dateTime.getMinute() != 0 && dateTime.getMinute() != 30)
                || dateTime.getSecond() != 0 || dateTime.getNano() != 0) {
            throw new BusinessRuleException("Las citas sólo se agendan en horas en punto o y media (:00 o :30)");
        }
        // RN2: lunes a viernes, 8:00 a 16:30 (última cita)
        DayOfWeek day = dateTime.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new BusinessRuleException("Sólo se agenda de lunes a viernes");
        }
        LocalTime time = dateTime.toLocalTime();
        if (time.isBefore(FIRST_SLOT) || time.isAfter(LAST_SLOT)) {
            throw new BusinessRuleException("El horario de atención es de 8:00 a 17:00 (última cita 16:30)");
        }
        // RN3: no en el pasado
        if (!dateTime.isAfter(LocalDateTime.now(clock))) {
            throw new BusinessRuleException("No se puede agendar una cita en el pasado");
        }
    }

    private void validatePatientCanSchedule(PatientEntity patient) {
        if (patient.getNoShowCount() >= MAX_NO_SHOWS) {
            throw new BusinessRuleException("El paciente " + patient.getDocument()
                    + " tiene " + patient.getNoShowCount() + " inasistencias y no puede agendar");
        }
    }

    private void validateDoctorAvailable(DoctorEntity doctor, LocalDateTime dateTime) {
        if (appointmentRepository.existsByDoctorAndDateTimeAndStatus(doctor, dateTime, AppointmentStatus.SCHEDULED)) {
            throw new ConflictException("El médico " + doctor.getLicense() + " ya tiene una cita en ese horario");
        }
    }

    private void validatePatientHasNoAppointmentThatDay(PatientEntity patient, LocalDateTime dateTime) {
        LocalDateTime start = dateTime.toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        if (appointmentRepository.existsPatientAppointmentInRange(patient, AppointmentStatus.SCHEDULED, start, end)) {
            throw new ConflictException("El paciente " + patient.getDocument() + " ya tiene una cita ese día");
        }
    }

    // RN6
    private Double calculateAmount(Double fee, InsuranceType insuranceType) {
        return switch (insuranceType) {
            case NONE -> fee;
            case BASIC -> fee * 0.5;
            case FULL -> 0.0;
        };
    }

    private String generateNumber() {
        return "CITA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private AppointmentEntity findEntity(String number) {
        return appointmentRepository.findByNumber(number)
                .orElseThrow(() -> new NotFoundException("No existe una cita con número " + number));
    }

    // Máquina de estados: sólo se puede salir de SCHEDULED
    private AppointmentEntity findScheduled(String number, String action) {
        AppointmentEntity entity = findEntity(number);
        if (entity.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessRuleException("No se puede " + action + " una cita en estado " + entity.getStatus());
        }
        return entity;
    }
}
