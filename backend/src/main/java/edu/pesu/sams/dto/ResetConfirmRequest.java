package edu.pesu.sams.dto;
import jakarta.validation.constraints.*;
public record ResetConfirmRequest(@NotBlank String token, @NotBlank @Size(min=8,max=64) String newPassword) {}
