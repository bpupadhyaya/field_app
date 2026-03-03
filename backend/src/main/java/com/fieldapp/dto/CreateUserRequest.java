package com.fieldapp.dto;
import com.fieldapp.model.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String displayName,
        @NotBlank @Email String email,
        UserType userType,
        @NotEmpty Set<String> roles,
        Long managerId
) {}
