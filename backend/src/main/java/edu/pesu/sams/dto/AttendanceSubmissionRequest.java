package edu.pesu.sams.dto;
import edu.pesu.sams.entity.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record AttendanceSubmissionRequest(@NotEmpty List<@Valid AttendanceMark> records) {
 public record AttendanceMark(@NotBlank String studentId, @NotNull AttendanceStatus status) {}
}
