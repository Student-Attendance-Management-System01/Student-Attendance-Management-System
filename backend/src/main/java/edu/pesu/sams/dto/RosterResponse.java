package edu.pesu.sams.dto;
import edu.pesu.sams.entity.CourseSection;
import edu.pesu.sams.entity.Enrollment;
import java.util.List;
public record RosterResponse(Long courseSectionId, String courseCode, String courseName, String semester, String section, String academicYear, int totalStudents, List<RosterStudent> students) {
 public record RosterStudent(Long id, String institutionId, String fullName, String email, boolean active, String enrolledAt) {
  public static RosterStudent of(Enrollment e){return new RosterStudent(e.getStudent().getId(),e.getStudent().getInstitutionId(),e.getStudent().getFullName(),e.getStudent().getEmail(),e.getStudent().isActive(),String.valueOf(e.getCreatedAt()));}
 }
 public static RosterResponse of(CourseSection s,List<RosterStudent> students){return new RosterResponse(s.getId(),s.getCourseCode(),s.getCourseName(),s.getSemester(),s.getSection(),s.getAcademicYear(),students.size(),students);}
}
