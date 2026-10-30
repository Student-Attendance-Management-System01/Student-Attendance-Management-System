package edu.pesu.sams.dto;
import jakarta.validation.constraints.*;
/** Field limits follow SRS Appendix B.1 (user registration). */
public record CreateFacultyRequest(
 @NotBlank @Pattern(regexp="^[A-Za-z0-9]{1,20}$",message="must be 1-20 letters or digits (Employee ID)") String institutionId,
 @NotBlank @Size(max=60) String fullName,
 @NotBlank @Email @Size(max=100) String email,
 @NotBlank @Size(min=8,max=64) String password
) {}
