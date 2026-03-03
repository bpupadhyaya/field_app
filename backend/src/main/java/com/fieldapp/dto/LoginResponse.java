package com.fieldapp.dto;
import java.util.Set;
public record LoginResponse(String token, String username, String displayName, Set<String> roles) {}
