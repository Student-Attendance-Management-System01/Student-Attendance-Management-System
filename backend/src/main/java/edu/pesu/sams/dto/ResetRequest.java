package edu.pesu.sams.dto;
import jakarta.validation.constraints.*;
public record ResetRequest(@NotBlank @Email String email) {}
