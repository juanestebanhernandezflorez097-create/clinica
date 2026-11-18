package com.example.clinica.mappers;

import com.example.clinica.dtos.PatientCreateDto;
import com.example.clinica.dtos.PatientResponseDto;
import com.example.clinica.dtos.PatientUpdateDto;
import com.example.clinica.entities.PatientEntity;
import com.example.clinica.models.Patient;

public class PatientMapper {

    public static Patient entityToModel(PatientEntity entity) {
        Patient patient = new Patient();
        patient.setId(entity.getId());
        patient.setDocument(entity.getDocument());
        patient.setFullName(entity.getFullName());
        patient.setBirthDate(entity.getBirthDate());
        patient.setInsuranceType(entity.getInsuranceType());
        patient.setNoShowCount(entity.getNoShowCount());
        return patient;
    }

    public static PatientEntity modelToEntity(Patient patient) {
        PatientEntity entity = new PatientEntity();
        entity.setId(patient.getId());
        entity.setDocument(patient.getDocument());
        entity.setFullName(patient.getFullName());
        entity.setBirthDate(patient.getBirthDate());
        entity.setInsuranceType(patient.getInsuranceType());
        entity.setNoShowCount(patient.getNoShowCount());
        return entity;
    }

    public static PatientResponseDto modelToDto(Patient patient) {
        PatientResponseDto dto = new PatientResponseDto();
        dto.setId(patient.getId());
        dto.setDocument(patient.getDocument());
        dto.setFullName(patient.getFullName());
        dto.setBirthDate(patient.getBirthDate());
        dto.setInsuranceType(patient.getInsuranceType());
        dto.setNoShowCount(patient.getNoShowCount());
        return dto;
    }

    public static Patient createDtoToModel(PatientCreateDto dto) {
        Patient patient = new Patient();
        patient.setDocument(dto.getDocument());
        patient.setFullName(dto.getFullName());
        patient.setBirthDate(dto.getBirthDate());
        patient.setInsuranceType(dto.getInsuranceType());
        return patient;
    }

    public static Patient updateDtoToModel(PatientUpdateDto dto) {
        Patient patient = new Patient();
        patient.setFullName(dto.getFullName());
        patient.setBirthDate(dto.getBirthDate());
        patient.setInsuranceType(dto.getInsuranceType());
        return patient;
    }
}
