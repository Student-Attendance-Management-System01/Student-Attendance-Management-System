package edu.pesu.sams.dto;
import jakarta.validation.constraints.*;
/** The institution ID is the stable identifier and cannot be changed; status changes go through {@code active}. */
public record UpdateStudentRequest(@NotBlank @Size(max=60) String fullName, @NotBlank @Email @Size(max=100) String email, Boolean active) {}
