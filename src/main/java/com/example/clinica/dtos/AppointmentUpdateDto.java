package com.example.clinica.dtos;

import lombok.Data;

// Las citas no se editan por PUT: los cambios se hacen con cancel / attend / no-show.
// Se deja el DTO para respetar la convención <X>CreateDto, <X>UpdateDto, <X>ResponseDto.
@Data
public class AppointmentUpdateDto {
}
