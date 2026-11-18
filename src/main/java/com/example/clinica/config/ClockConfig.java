package com.example.clinica.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    // Se inyecta el reloj para poder fijar la hora "actual" en las pruebas.
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
