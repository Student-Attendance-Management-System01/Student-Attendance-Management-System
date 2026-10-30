package edu.pesu.sams.dto;
import jakarta.validation.constraints.*;
import java.util.List;
public record CourseSectionRequest(
 @NotBlank @Size(max=32) String courseCode,
 @NotBlank @Size(max=160) String courseName,
 @NotBlank @Size(max=24) String semester,
 @NotBlank @Size(max=24) String section,
 @NotBlank @Size(max=16) String academicYear,
 @NotEmpty List<@NotNull Long> facultyIds
) {}
