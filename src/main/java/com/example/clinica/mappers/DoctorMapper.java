package com.example.clinica.mappers;

import com.example.clinica.dtos.DoctorCreateDto;
import com.example.clinica.dtos.DoctorResponseDto;
import com.example.clinica.dtos.DoctorUpdateDto;
import com.example.clinica.entities.DoctorEntity;
import com.example.clinica.models.Doctor;

public class DoctorMapper {

    public static Doctor entityToModel(DoctorEntity entity) {
        Doctor doctor = new Doctor();
        doctor.setId(entity.getId());
        doctor.setLicense(entity.getLicense());
        doctor.setFullName(entity.getFullName());
        doctor.setSpecialty(entity.getSpecialty());
        doctor.setConsultationFee(entity.getConsultationFee());
        return doctor;
    }

    public static DoctorEntity modelToEntity(Doctor doctor) {
        DoctorEntity entity = new DoctorEntity();
        entity.setId(doctor.getId());
        entity.setLicense(doctor.getLicense());
        entity.setFullName(doctor.getFullName());
        entity.setSpecialty(doctor.getSpecialty());
        entity.setConsultationFee(doctor.getConsultationFee());
        return entity;
    }

    public static DoctorResponseDto modelToDto(Doctor doctor) {
        DoctorResponseDto dto = new DoctorResponseDto();
        dto.setId(doctor.getId());
        dto.setLicense(doctor.getLicense());
        dto.setFullName(doctor.getFullName());
        dto.setSpecialty(doctor.getSpecialty());
        dto.setConsultationFee(doctor.getConsultationFee());
        return dto;
    }

    public static Doctor createDtoToModel(DoctorCreateDto dto) {
        Doctor doctor = new Doctor();
        doctor.setLicense(dto.getLicense());
        doctor.setFullName(dto.getFullName());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setConsultationFee(dto.getConsultationFee());
        return doctor;
    }

    public static Doctor updateDtoToModel(DoctorUpdateDto dto) {
        Doctor doctor = new Doctor();
        doctor.setFullName(dto.getFullName());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setConsultationFee(dto.getConsultationFee());
        return doctor;
    }
}
