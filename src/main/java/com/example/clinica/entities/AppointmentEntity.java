package com.example.clinica.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.clinica.models.AppointmentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "appointments")
@Data
public class AppointmentEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String number;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id")
    private DoctorEntity doctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private PatientEntity patient;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Column(nullable = false)
    private Double amountToPay;
}
