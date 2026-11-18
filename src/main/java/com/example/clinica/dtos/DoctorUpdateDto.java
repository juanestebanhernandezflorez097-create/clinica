package com.example.clinica.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class DoctorUpdateDto {
    @NotBlank(message = "El nombre es obligatorio")
    private String fullName;

    @NotBlank(message = "La especialidad es obligatoria")
    private String specialty;

    @NotNull(message = "La tarifa es obligatoria")
    @PositiveOrZero(message = "La tarifa no puede ser negativa")
    private Double consultationFee;
}
