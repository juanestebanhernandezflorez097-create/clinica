package com.example.clinica.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.clinica.entities.DoctorEntity;

@Repository
public interface DoctorRepository extends JpaRepository<DoctorEntity, UUID> {

    Optional<DoctorEntity> findByLicense(String license);

    boolean existsByLicense(String license);
}
