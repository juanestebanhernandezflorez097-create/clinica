package com.example.clinica.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.clinica.entities.DoctorEntity;
import com.example.clinica.exceptions.BusinessRuleException;
import com.example.clinica.exceptions.ConflictException;
import com.example.clinica.exceptions.NotFoundException;
import com.example.clinica.mappers.DoctorMapper;
import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.models.Doctor;
import com.example.clinica.repositories.AppointmentRepository;
import com.example.clinica.repositories.DoctorRepository;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<Doctor> getAll() {
        return doctorRepository.findAll().stream().map(DoctorMapper::entityToModel).toList();
    }

    public Doctor getByLicense(String license) {
        return DoctorMapper.entityToModel(findEntity(license));
    }

    @Transactional
    public Doctor create(Doctor doctor) {
        if (doctorRepository.existsByLicense(doctor.getLicense())) {
            throw new ConflictException("Ya existe un médico con licencia " + doctor.getLicense());
        }
        DoctorEntity saved = doctorRepository.save(DoctorMapper.modelToEntity(doctor));
        return DoctorMapper.entityToModel(saved);
    }

    @Transactional
    public Doctor update(String license, Doctor doctor) {
        DoctorEntity entity = findEntity(license);
        entity.setFullName(doctor.getFullName());
        entity.setSpecialty(doctor.getSpecialty());
        entity.setConsultationFee(doctor.getConsultationFee());
        return DoctorMapper.entityToModel(doctorRepository.save(entity));
    }

    @Transactional
    public void delete(String license) {
        DoctorEntity entity = findEntity(license);
        if (appointmentRepository.existsByDoctor_LicenseAndStatus(license, AppointmentStatus.SCHEDULED)) {
            throw new BusinessRuleException("El médico " + license + " tiene citas programadas y no se puede eliminar");
        }
        doctorRepository.delete(entity);
    }

    private DoctorEntity findEntity(String license) {
        return doctorRepository.findByLicense(license)
                .orElseThrow(() -> new NotFoundException("No existe un médico con licencia " + license));
    }
}
