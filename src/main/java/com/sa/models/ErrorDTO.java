package com.sa.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ErrorDTO(int status, String message, String timestamp) {
    public ErrorDTO(int status, String message){
        this(status, message, LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}