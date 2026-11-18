package com.example.clinica.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentCreateDto {
    @NotBlank(message = "La licencia del médico es obligatoria")
    private String doctorLicense;

    @NotBlank(message = "El documento del paciente es obligatorio")
    private String patientDocument;

    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime dateTime;
}
