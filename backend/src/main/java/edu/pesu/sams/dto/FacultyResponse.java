package edu.pesu.sams.dto;
import edu.pesu.sams.entity.UserAccount;
import java.util.List;
public record FacultyResponse(Long id, String institutionId, String fullName, String email, boolean active, List<SectionSummary> sections) {
 public record SectionSummary(Long id, String courseCode, String section, String semester, String academicYear) {}
 public static FacultyResponse of(UserAccount u,List<SectionSummary> sections){return new FacultyResponse(u.getId(),u.getInstitutionId(),u.getFullName(),u.getEmail(),u.isActive(),sections);}
}
