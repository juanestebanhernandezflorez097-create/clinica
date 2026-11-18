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

import com.example.clinica.dtos.PatientCreateDto;
import com.example.clinica.dtos.PatientResponseDto;
import com.example.clinica.dtos.PatientUpdateDto;
import com.example.clinica.mappers.PatientMapper;
import com.example.clinica.models.Patient;
import com.example.clinica.services.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<List<PatientResponseDto>> getAll() {
        List<PatientResponseDto> patients = patientService.getAll().stream().map(PatientMapper::modelToDto).toList();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{document}")
    public ResponseEntity<PatientResponseDto> getByDocument(@PathVariable("document") String document) {
        Patient patient = patientService.getByDocument(document);
        return ResponseEntity.ok(PatientMapper.modelToDto(patient));
    }

    @PostMapping
    public ResponseEntity<PatientResponseDto> create(@Valid @RequestBody PatientCreateDto createDto) {
        Patient patient = PatientMapper.createDtoToModel(createDto);
        Patient created = patientService.create(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(PatientMapper.modelToDto(created));
    }

    @PutMapping("/{document}")
    public ResponseEntity<PatientResponseDto> update(@PathVariable("document") String document,
            @Valid @RequestBody PatientUpdateDto updateDto) {
        Patient patient = PatientMapper.updateDtoToModel(updateDto);
        Patient updated = patientService.update(document, patient);
        return ResponseEntity.ok(PatientMapper.modelToDto(updated));
    }

    @DeleteMapping("/{document}")
    public ResponseEntity<Void> delete(@PathVariable("document") String document) {
        patientService.delete(document);
        return ResponseEntity.noContent().build();
    }
}
