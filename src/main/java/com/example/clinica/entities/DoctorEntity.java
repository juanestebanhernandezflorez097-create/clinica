package com.example.clinica.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "doctors")
@Data
public class DoctorEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String license;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String specialty;

    @Column(nullable = false)
    private Double consultationFee;
}
