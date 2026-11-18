package com.example.clinica.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.clinica.dtos.DoctorCreateDto;
import com.example.clinica.dtos.DoctorResponseDto;
import com.example.clinica.dtos.DoctorUpdateDto;
import com.example.clinica.mappers.DoctorMapper;
import com.example.clinica.models.Doctor;
import com.example.clinica.services.DoctorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponseDto>> getAll() {
        List<DoctorResponseDto> doctors = doctorService.getAll().stream().map(DoctorMapper::modelToDto).toList();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{license}")
    public ResponseEntity<DoctorResponseDto> getByLicense(@PathVariable("license") String license) {
        Doctor doctor = doctorService.getByLicense(license);
        return ResponseEntity.ok(DoctorMapper.modelToDto(doctor));
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDto> create(@Valid @RequestBody DoctorCreateDto createDto) {
        Doctor doctor = DoctorMapper.createDtoToModel(createDto);
        Doctor created = doctorService.create(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(DoctorMapper.modelToDto(created));
    }

    @PutMapping("/{license}")
    public ResponseEntity<DoctorResponseDto> update(@PathVariable("license") String license,
            @Valid @RequestBody DoctorUpdateDto updateDto) {
        Doctor doctor = DoctorMapper.updateDtoToModel(updateDto);
        Doctor updated = doctorService.update(license, doctor);
        return ResponseEntity.ok(DoctorMapper.modelToDto(updated));
    }

    @DeleteMapping("/{license}")
    public ResponseEntity<Void> delete(@PathVariable("license") String license) {
        doctorService.delete(license);
        return ResponseEntity.noContent().build();
    }
}
