package com.example.clinica.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.clinica.entities.AppointmentEntity;
import com.example.clinica.entities.DoctorEntity;
import com.example.clinica.entities.PatientEntity;
import com.example.clinica.models.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {

    Optional<AppointmentEntity> findByNumber(String number);

    // RN4: el médico ya tiene una cita en ese horario
    boolean existsByDoctorAndDateTimeAndStatus(DoctorEntity doctor, LocalDateTime dateTime,
            AppointmentStatus status);

    // RN5: el paciente ya tiene una cita ese día
    @Query("select (count(a) > 0) from AppointmentEntity a "
            + "where a.patient = :patient and a.status = :status "
            + "and a.dateTime >= :start and a.dateTime < :end")
    boolean existsPatientAppointmentInRange(@Param("patient") PatientEntity patient,
            @Param("status") AppointmentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Para rechazar el borrado de un médico con citas SCHEDULED
    boolean existsByDoctor_LicenseAndStatus(String license, AppointmentStatus status);

    // Parte 5: citas de un médico en un día
    List<AppointmentEntity> findByDoctor_LicenseAndDateTimeBetweenOrderByDateTime(
            String license, LocalDateTime start, LocalDateTime end);

    // Parte 5: historial de un paciente
    List<AppointmentEntity> findByPatient_DocumentOrderByDateTimeDesc(String document);
}
