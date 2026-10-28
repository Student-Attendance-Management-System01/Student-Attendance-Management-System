package edu.pesu.sams.dto;
import jakarta.validation.constraints.*;
import edu.pesu.sams.entity.AttendanceStatus;
public record CorrectionRequestCreate(@NotNull Long attendanceRecordId, @NotNull AttendanceStatus requestedStatus, @NotBlank @Size(max=250) String reason) {}
