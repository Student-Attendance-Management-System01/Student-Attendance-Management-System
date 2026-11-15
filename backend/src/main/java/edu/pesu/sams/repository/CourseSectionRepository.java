package edu.pesu.sams.repository;
import edu.pesu.sams.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CourseSectionRepository extends JpaRepository<CourseSection,Long> {
 List<CourseSection> findByActiveTrueOrderByCourseCodeAsc();
 List<CourseSection> findDistinctByFaculty_EmailIgnoreCaseAndActiveTrue(String email);
 List<CourseSection> findDistinctByFaculty_IdOrderByCourseCodeAsc(Long facultyId);
 Optional<CourseSection> findByCourseCodeAndSemesterAndSectionAndAcademicYear(String courseCode,String semester,String section,String academicYear);
}
