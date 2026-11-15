package edu.pesu.sams.dto;
import edu.pesu.sams.entity.CourseSection;
import edu.pesu.sams.entity.UserAccount;
import java.util.Comparator;
import java.util.List;
public record CourseSectionResponse(Long id, String courseCode, String courseName, String semester, String section, String academicYear, boolean active, List<FacultySummary> faculty) {
 public record FacultySummary(Long id, String institutionId, String fullName) {}
 public static CourseSectionResponse of(CourseSection s){
   List<FacultySummary> faculty=s.getFaculty().stream().sorted(Comparator.comparing(UserAccount::getInstitutionId)).map(u->new FacultySummary(u.getId(),u.getInstitutionId(),u.getFullName())).toList();
   return new CourseSectionResponse(s.getId(),s.getCourseCode(),s.getCourseName(),s.getSemester(),s.getSection(),s.getAcademicYear(),s.isActive(),faculty);
 }
}
