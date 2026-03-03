package com.fieldapp.dto;
import jakarta.validation.constraints.NotBlank;
public record ControlCommandRequest(@NotBlank String command, String zoneId) {}
