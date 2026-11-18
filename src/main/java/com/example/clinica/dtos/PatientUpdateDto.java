package com.example.clinica.dtos;

import java.time.LocalDate;

import com.example.clinica.models.InsuranceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

@Data
public class PatientUpdateDto {
    @NotBlank(message = "El nombre es obligatorio")
    private String fullName;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe estar en el pasado")
    private LocalDate birthDate;

    @NotNull(message = "El tipo de seguro es obligatorio (NONE, BASIC, FULL)")
    private InsuranceType insuranceType;
}
