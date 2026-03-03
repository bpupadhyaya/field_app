package com.fieldapp.dto;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public record UpdatePriceRequest(@NotNull BigDecimal price) {}
