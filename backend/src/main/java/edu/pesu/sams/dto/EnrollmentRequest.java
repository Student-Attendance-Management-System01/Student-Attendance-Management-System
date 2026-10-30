package edu.pesu.sams.dto;
import jakarta.validation.constraints.NotBlank;
public record EnrollmentRequest(@NotBlank String institutionId) {}
