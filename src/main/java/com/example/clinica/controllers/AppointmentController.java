package com.example.clinica.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.clinica.dtos.AppointmentCreateDto;
import com.example.clinica.dtos.AppointmentResponseDto;
import com.example.clinica.mappers.AppointmentMapper;
import com.example.clinica.models.Appointment;
import com.example.clinica.services.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> create(@Valid @RequestBody AppointmentCreateDto createDto) {
        Appointment appointment = AppointmentMapper.createDtoToModel(createDto);
        Appointment created = appointmentService.create(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentMapper.modelToDto(created));
    }

    @GetMapping("/{number}")
    public ResponseEntity<AppointmentResponseDto> getByNumber(@PathVariable("number") String number) {
        return ResponseEntity.ok(AppointmentMapper.modelToDto(appointmentService.getByNumber(number)));
    }

    // GET /api/appointment?doctorLicense=MED-100&date=2026-12-01
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDto>> getByDoctorAndDate(
            @RequestParam("doctorLicense") String doctorLicense,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentResponseDto> appointments = appointmentService.getByDoctorAndDate(doctorLicense, date)
                .stream().map(AppointmentMapper::modelToDto).toList();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/patient/{document}")
    public ResponseEntity<List<AppointmentResponseDto>> getByPatient(@PathVariable("document") String document) {
        List<AppointmentResponseDto> appointments = appointmentService.getByPatient(document)
                .stream().map(AppointmentMapper::modelToDto).toList();
        return ResponseEntity.ok(appointments);
    }

    @PatchMapping("/{number}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancel(@PathVariable("number") String number) {
        return ResponseEntity.ok(AppointmentMapper.modelToDto(appointmentService.cancel(number)));
    }

    @PatchMapping("/{number}/attend")
    public ResponseEntity<AppointmentResponseDto> attend(@PathVariable("number") String number) {
        return ResponseEntity.ok(AppointmentMapper.modelToDto(appointmentService.attend(number)));
    }

    @PatchMapping("/{number}/no-show")
    public ResponseEntity<AppointmentResponseDto> noShow(@PathVariable("number") String number) {
        return ResponseEntity.ok(AppointmentMapper.modelToDto(appointmentService.noShow(number)));
    }
}
