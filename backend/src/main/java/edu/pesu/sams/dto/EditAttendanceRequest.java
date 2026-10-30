package edu.pesu.sams.dto;
import edu.pesu.sams.entity.AttendanceStatus;
import jakarta.validation.constraints.*;
public record EditAttendanceRequest(@NotNull AttendanceStatus status, @NotBlank @Size(min=10,max=250) String reason) {}
