package edu.pesu.sams.dto;
import jakarta.validation.constraints.*;
import java.util.List;
/** Same limits as {@link CourseSectionRequest}; {@code active} null leaves the status unchanged. */
public record UpdateCourseSectionRequest(
 @NotBlank @Pattern(regexp="^[A-Za-z0-9]{1,10}$",message="must be 1-10 letters or digits") String courseCode,
 @NotBlank @Size(max=80) String courseName,
 @NotBlank @Pattern(regexp="^[1-8]$",message="must be a number from 1 to 8") String semester,
 @NotBlank @Pattern(regexp="^[A-Za-z0-9]{1,2}$",message="must be 1-2 letters or digits") String section,
 @NotBlank @Pattern(regexp="^\\d{4}-\\d{2}$",message="must look like 2026-27") String academicYear,
 @NotEmpty List<@NotNull Long> facultyIds,
 Boolean active
) {}
