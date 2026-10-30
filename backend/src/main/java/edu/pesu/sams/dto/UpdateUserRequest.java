package edu.pesu.sams.dto;
import edu.pesu.sams.entity.UserRole;
import jakarta.validation.constraints.*;
public record UpdateUserRequest(@NotBlank @Size(max=160) String fullName, @NotBlank @Email @Size(max=190) String email, @NotNull UserRole role, Boolean active) {}
