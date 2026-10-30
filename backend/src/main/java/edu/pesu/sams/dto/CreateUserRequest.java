package edu.pesu.sams.dto;
import edu.pesu.sams.entity.UserRole;
import jakarta.validation.constraints.*;
public record CreateUserRequest(
 @NotBlank @Size(max=64) String institutionId,
 @NotBlank @Size(max=160) String fullName,
 @NotBlank @Email @Size(max=190) String email,
 @NotNull UserRole role,
 @NotBlank @Size(min=8,max=64) String password
) {}
