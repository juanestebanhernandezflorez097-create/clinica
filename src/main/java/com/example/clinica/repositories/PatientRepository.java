package com.example.clinica.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.clinica.entities.PatientEntity;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, UUID> {

    Optional<PatientEntity> findByDocument(String document);

    boolean existsByDocument(String document);
}
