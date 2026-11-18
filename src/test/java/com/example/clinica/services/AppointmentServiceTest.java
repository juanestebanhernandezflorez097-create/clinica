package com.example.clinica.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.example.clinica.entities.AppointmentEntity;
import com.example.clinica.entities.DoctorEntity;
import com.example.clinica.entities.PatientEntity;
import com.example.clinica.exceptions.BusinessRuleException;
import com.example.clinica.exceptions.ConflictException;
import com.example.clinica.exceptions.NotFoundException;
import com.example.clinica.models.Appointment;
import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.models.Doctor;
import com.example.clinica.models.InsuranceType;
import com.example.clinica.models.Patient;
import com.example.clinica.repositories.AppointmentRepository;
import com.example.clinica.repositories.DoctorRepository;
import com.example.clinica.repositories.PatientRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppointmentServiceTest {

    // "Hoy" fijo: jueves 12/11/2026 10:00. El lunes siguiente es 16/11/2026.
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 12, 10, 0);
    private static final LocalDateTime MONDAY = LocalDateTime.of(2026, 11, 16, 9, 0);

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;

    private AppointmentService service;
    private DoctorEntity doctor;

    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.systemDefault();
        Clock clock = Clock.fixed(NOW.atZone(zone).toInstant(), zone);
        service = new AppointmentService(appointmentRepository, doctorRepository, patientRepository, clock);

        doctor = new DoctorEntity();
        doctor.setLicense("MED-100");
        doctor.setFullName("Dra. Pérez");
        doctor.setSpecialty("General");
        doctor.setConsultationFee(100000.0);
        when(doctorRepository.findByLicense("MED-100")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.save(any(AppointmentEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(patientRepository.save(any(PatientEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---------- helpers
    private PatientEntity patient(String document, InsuranceType insurance, int noShows) {
        PatientEntity p = new PatientEntity();
        p.setDocument(document);
        p.setFullName("Paciente " + document);
        p.setInsuranceType(insurance);
        p.setNoShowCount(noShows);
        when(patientRepository.findByDocument(document)).thenReturn(Optional.of(p));
        return p;
    }

    private Appointment request(String license, String document, LocalDateTime dateTime) {
        Doctor d = new Doctor();
        d.setLicense(license);
        Patient p = new Patient();
        p.setDocument(document);
        Appointment a = new Appointment();
        a.setDoctor(d);
        a.setPatient(p);
        a.setDateTime(dateTime);
        return a;
    }

    private AppointmentEntity storedAppointment(PatientEntity p, LocalDateTime dateTime, AppointmentStatus status) {
        AppointmentEntity e = new AppointmentEntity();
        e.setNumber("CITA-TEST");
        e.setDoctor(doctor);
        e.setPatient(p);
        e.setDateTime(dateTime);
        e.setStatus(status);
        e.setAmountToPay(100000.0);
        when(appointmentRepository.findByNumber("CITA-TEST")).thenReturn(Optional.of(e));
        return e;
    }

    // ---------- RN6: valor a pagar
    @Test
    void createsAppointmentWithoutInsurancePaysFullFee() {
        patient("P-1", InsuranceType.NONE, 0);
        Appointment result = service.create(request("MED-100", "P-1", MONDAY));
        assertEquals(100000.0, result.getAmountToPay());
        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
    }

    @Test
    void basicInsurancePaysHalf() {
        patient("P-2", InsuranceType.BASIC, 0);
        Appointment result = service.create(request("MED-100", "P-2", MONDAY.withMinute(30)));
        assertEquals(50000.0, result.getAmountToPay());
    }

    @Test
    void fullInsurancePaysNothing() {
        patient("P-3", InsuranceType.FULL, 0);
        Appointment result = service.create(request("MED-100", "P-3", MONDAY.withHour(10)));
        assertEquals(0.0, result.getAmountToPay());
    }

    @Test
    void generatesAppointmentNumber() {
        patient("P-1", InsuranceType.NONE, 0);
        Appointment result = service.create(request("MED-100", "P-1", MONDAY));
        assertEquals(true, result.getNumber().startsWith("CITA-"));
    }

    // ---------- RN1, RN2, RN3
    @Test
    void rejectsInvalidMinutes() {
        patient("P-1", InsuranceType.NONE, 0);
        assertThrows(BusinessRuleException.class,
                () -> service.create(request("MED-100", "P-1", MONDAY.withMinute(15))));
    }

    @Test
    void rejectsSaturday() {
        patient("P-1", InsuranceType.NONE, 0);
        assertThrows(BusinessRuleException.class,
                () -> service.create(request("MED-100", "P-1", MONDAY.plusDays(5))));
    }

    @Test
    void rejectsAt1700() {
        patient("P-1", InsuranceType.NONE, 0);
        assertThrows(BusinessRuleException.class,
                () -> service.create(request("MED-100", "P-1", MONDAY.withHour(17))));
    }

    @Test
    void rejectsAt0730() {
        patient("P-1", InsuranceType.NONE, 0);
        assertThrows(BusinessRuleException.class,
                () -> service.create(request("MED-100", "P-1", MONDAY.withHour(7).withMinute(30))));
    }

    @Test
    void acceptsLastSlotAt1630() {
        patient("P-1", InsuranceType.NONE, 0);
        Appointment result = service.create(request("MED-100", "P-1", MONDAY.withHour(16).withMinute(30)));
        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
    }

    @Test
    void rejectsPast() {
        patient("P-1", InsuranceType.NONE, 0);
        assertThrows(BusinessRuleException.class,
                () -> service.create(request("MED-100", "P-1", NOW.minusDays(1).withMinute(0))));
    }

    // ---------- RN4, RN5, RN7
    @Test
    void rejectsBusyDoctor() {
        patient("P-4", InsuranceType.NONE, 0);
        when(appointmentRepository.existsByDoctorAndDateTimeAndStatus(any(), any(), any())).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.create(request("MED-100", "P-4", MONDAY)));
    }

    @Test
    void rejectsPatientWithAnotherAppointmentSameDay() {
        patient("P-1", InsuranceType.NONE, 0);
        when(appointmentRepository.existsPatientAppointmentInRange(any(), any(), any(), any())).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> service.create(request("MED-100", "P-1", MONDAY.withHour(11))));
    }

    @Test
    void rejectsPatientWithTwoNoShows() {
        patient("P-5", InsuranceType.NONE, 2);
        assertThrows(BusinessRuleException.class, () -> service.create(request("MED-100", "P-5", MONDAY)));
    }

    @Test
    void unknownDoctorIsNotFound() {
        patient("P-1", InsuranceType.NONE, 0);
        when(doctorRepository.findByLicense("MED-999")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(request("MED-999", "P-1", MONDAY)));
    }

    // ---------- RN8 y máquina de estados
    @Test
    void cancelsWhenMoreThanTwoHoursAway() {
        PatientEntity p = patient("P-1", InsuranceType.NONE, 0);
        storedAppointment(p, NOW.plusHours(3), AppointmentStatus.SCHEDULED);
        assertEquals(AppointmentStatus.CANCELLED, service.cancel("CITA-TEST").getStatus());
    }

    @Test
    void rejectsCancelOneHourBefore() {
        PatientEntity p = patient("P-1", InsuranceType.NONE, 0);
        storedAppointment(p, NOW.plusHours(1), AppointmentStatus.SCHEDULED);
        assertThrows(BusinessRuleException.class, () -> service.cancel("CITA-TEST"));
    }

    @Test
    void rejectsCancelExactlyTwoHoursBefore() {
        PatientEntity p = patient("P-1", InsuranceType.NONE, 0);
        storedAppointment(p, NOW.plusHours(2), AppointmentStatus.SCHEDULED);
        assertThrows(BusinessRuleException.class, () -> service.cancel("CITA-TEST"));
    }

    @Test
    void rejectsAttendingCancelledAppointment() {
        PatientEntity p = patient("P-1", InsuranceType.NONE, 0);
        storedAppointment(p, NOW.plusDays(3), AppointmentStatus.CANCELLED);
        assertThrows(BusinessRuleException.class, () -> service.attend("CITA-TEST"));
    }

    @Test
    void attendMarksAttended() {
        PatientEntity p = patient("P-1", InsuranceType.NONE, 0);
        storedAppointment(p, NOW.plusDays(3), AppointmentStatus.SCHEDULED);
        assertEquals(AppointmentStatus.ATTENDED, service.attend("CITA-TEST").getStatus());
    }

    @Test
    void noShowIncrementsPatientCounter() {
        PatientEntity p = patient("P-1", InsuranceType.NONE, 0);
        storedAppointment(p, NOW.plusDays(3), AppointmentStatus.SCHEDULED);
        Appointment result = service.noShow("CITA-TEST");
        assertEquals(AppointmentStatus.NO_SHOW, result.getStatus());
        assertEquals(1, p.getNoShowCount());
    }

    @Test
    void finalStatesCannotChange() {
        PatientEntity p = patient("P-1", InsuranceType.NONE, 0);
        storedAppointment(p, NOW.plusDays(3), AppointmentStatus.NO_SHOW);
        assertThrows(BusinessRuleException.class, () -> service.cancel("CITA-TEST"));
        assertThrows(BusinessRuleException.class, () -> service.attend("CITA-TEST"));
        assertThrows(BusinessRuleException.class, () -> service.noShow("CITA-TEST"));
    }

    @Test
    void unknownAppointmentIsNotFound() {
        when(appointmentRepository.findByNumber("NOPE")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getByNumber("NOPE"));
    }
}
