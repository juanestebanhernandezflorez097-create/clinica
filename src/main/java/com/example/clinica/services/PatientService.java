package com.example.clinica.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.clinica.entities.PatientEntity;
import com.example.clinica.exceptions.ConflictException;
import com.example.clinica.exceptions.NotFoundException;
import com.example.clinica.mappers.PatientMapper;
import com.example.clinica.models.Patient;
import com.example.clinica.repositories.PatientRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> getAll() {
        return patientRepository.findAll().stream().map(PatientMapper::entityToModel).toList();
    }

    public Patient getByDocument(String document) {
        return PatientMapper.entityToModel(findEntity(document));
    }

    @Transactional
    public Patient create(Patient patient) {
        if (patientRepository.existsByDocument(patient.getDocument())) {
            throw new ConflictException("Ya existe un paciente con documento " + patient.getDocument());
        }
        patient.setNoShowCount(0); // nunca viene del cliente
        PatientEntity saved = patientRepository.save(PatientMapper.modelToEntity(patient));
        return PatientMapper.entityToModel(saved);
    }

    @Transactional
    public Patient update(String document, Patient patient) {
        PatientEntity entity = findEntity(document);
        entity.setFullName(patient.getFullName());
        entity.setBirthDate(patient.getBirthDate());
        entity.setInsuranceType(patient.getInsuranceType());
        return PatientMapper.entityToModel(patientRepository.save(entity));
    }

    @Transactional
    public void delete(String document) {
        patientRepository.delete(findEntity(document));
    }

    private PatientEntity findEntity(String document) {
        return patientRepository.findByDocument(document)
                .orElseThrow(() -> new NotFoundException("No existe un paciente con documento " + document));
    }
}
