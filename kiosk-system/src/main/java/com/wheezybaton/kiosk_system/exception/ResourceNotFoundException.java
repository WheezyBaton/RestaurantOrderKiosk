package com.wheezybaton.kiosk_system.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
        log.error("ResourceNotFoundException initialized with message: {}", message);
    }
}