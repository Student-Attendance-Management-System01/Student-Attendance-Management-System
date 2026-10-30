package edu.pesu.sams.service;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.exception.SamsException;
import edu.pesu.sams.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Course-sections (SAMS-F-012): unique by course code, semester, section and academic year, with at least one active Faculty (BR-7). */
@Service
public class CourseSectionService {
 private final CourseSectionRepository sections; private final UserAccountRepository users; private final AuditService audit;
 public CourseSectionService(CourseSectionRepository sections,UserAccountRepository users,AuditService audit){this.sections=sections;this.users=users;this.audit=audit;}
 @Transactional(readOnly=true) public CourseSectionResponse get(Long id){return CourseSectionResponse.of(require(id));}
 @Transactional public CourseSectionResponse create(CourseSectionRequest r,String actor,HttpServletRequest req){
   CourseSection s=new CourseSection();
   apply(s,r.courseCode(),r.courseName(),r.semester(),r.section(),r.academicYear(),r.facultyIds());sections.save(s);
   audit.record(actor,"COURSE_SECTION_CREATED","COURSE_SECTION",String.valueOf(s.getId()),null,label(s),null,req);return CourseSectionResponse.of(s);
 }
 @Transactional public CourseSectionResponse update(Long id,UpdateCourseSectionRequest r,String actor,HttpServletRequest req){
   CourseSection s=require(id);String old=label(s)+"|"+s.isActive();
   apply(s,r.courseCode(),r.courseName(),r.semester(),r.section(),r.academicYear(),r.facultyIds());if(r.active()!=null)s.setActive(r.active());sections.save(s);
   audit.record(actor,"COURSE_SECTION_UPDATED","COURSE_SECTION",String.valueOf(s.getId()),old,label(s)+"|"+s.isActive(),null,req);return CourseSectionResponse.of(s);
 }
 private void apply(CourseSection s,String courseCode,String courseName,String semester,String section,String academicYear,List<Long> facultyIds){
   String code=courseCode.trim().toUpperCase(Locale.ROOT),sem=semester.trim(),sec=section.trim().toUpperCase(Locale.ROOT),year=academicYear.trim();
   int start=Integer.parseInt(year.substring(0,4)),end=Integer.parseInt(year.substring(5));
   if(end!=(start+1)%100)throw SamsException.badRequest("academicYear must span consecutive years, e.g. 2026-27");
   sections.findByCourseCodeAndSemesterAndSectionAndAcademicYear(code,sem,sec,year).filter(other->!other.getId().equals(s.getId()))
     .ifPresent(other->{throw SamsException.conflict("SAMS-CRS-409","A course section with this course code, semester, section and academic year already exists");});
   Set<UserAccount> faculty=new HashSet<>();
   for(Long fid:new LinkedHashSet<>(facultyIds)){
     UserAccount u=users.findById(fid).orElseThrow(()->SamsException.badRequest("Faculty user not found: "+fid));
     if(u.getRole()!=UserRole.FACULTY||!u.isActive())throw SamsException.badRequest("Every assigned user must be an active Faculty member");
     faculty.add(u);
   }
   s.setCourseCode(code);s.setCourseName(courseName.trim());s.setSemester(sem);s.setSection(sec);s.setAcademicYear(year);s.setFaculty(faculty);
 }
 private CourseSection require(Long id){return sections.findById(id).orElseThrow(()->SamsException.notFound("Course section not found"));}
 private static String label(CourseSection s){return s.getCourseCode()+"/"+s.getSemester()+"/"+s.getSection()+"/"+s.getAcademicYear();}
}
