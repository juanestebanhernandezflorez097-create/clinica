package com.example.clinica.mappers;

import com.example.clinica.dtos.AppointmentCreateDto;
import com.example.clinica.dtos.AppointmentResponseDto;
import com.example.clinica.entities.AppointmentEntity;
import com.example.clinica.models.Appointment;
import com.example.clinica.models.Doctor;
import com.example.clinica.models.Patient;

public class AppointmentMapper {

    public static Appointment entityToModel(AppointmentEntity entity) {
        Appointment appointment = new Appointment();
        appointment.setId(entity.getId());
        appointment.setNumber(entity.getNumber());
        appointment.setDoctor(DoctorMapper.entityToModel(entity.getDoctor()));
        appointment.setPatient(PatientMapper.entityToModel(entity.getPatient()));
        appointment.setDateTime(entity.getDateTime());
        appointment.setStatus(entity.getStatus());
        appointment.setAmountToPay(entity.getAmountToPay());
        return appointment;
    }

    // Los datos de doctor/patient se completan en el servicio con las entidades ya cargadas.
    public static AppointmentEntity modelToEntity(Appointment appointment) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setId(appointment.getId());
        entity.setNumber(appointment.getNumber());
        entity.setDateTime(appointment.getDateTime());
        entity.setStatus(appointment.getStatus());
        entity.setAmountToPay(appointment.getAmountToPay());
        return entity;
    }

    public static AppointmentResponseDto modelToDto(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());
        dto.setNumber(appointment.getNumber());
        dto.setDoctorLicense(appointment.getDoctor().getLicense());
        dto.setDoctorName(appointment.getDoctor().getFullName());
        dto.setPatientDocument(appointment.getPatient().getDocument());
        dto.setPatientName(appointment.getPatient().getFullName());
        dto.setDateTime(appointment.getDateTime());
        dto.setStatus(appointment.getStatus());
        dto.setAmountToPay(appointment.getAmountToPay());
        return dto;
    }

    // El modelo sólo lleva la licencia y el documento; el servicio busca a las personas reales.
    public static Appointment createDtoToModel(AppointmentCreateDto dto) {
        Doctor doctor = new Doctor();
        doctor.setLicense(dto.getDoctorLicense());
        Patient patient = new Patient();
        patient.setDocument(dto.getPatientDocument());

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setDateTime(dto.getDateTime());
        return appointment;
    }
}
