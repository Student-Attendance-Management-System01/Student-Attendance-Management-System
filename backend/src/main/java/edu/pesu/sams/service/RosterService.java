package edu.pesu.sams.service;

import edu.pesu.sams.dto.RosterResponse;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.exception.SamsException;
import edu.pesu.sams.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Class rosters: admins see every section, faculty only the sections they are assigned to, students none. */
@Service
public class RosterService {
 private final CourseSectionRepository sections; private final EnrollmentRepository enrolments; private final UserAccountRepository users;
 public RosterService(CourseSectionRepository sections,EnrollmentRepository enrolments,UserAccountRepository users){this.sections=sections;this.enrolments=enrolments;this.users=users;}
 @Transactional(readOnly=true) public RosterResponse roster(Long sectionId,String email){
   UserAccount viewer=users.findByEmailIgnoreCase(email).orElseThrow(()->SamsException.unauthorized("Authentication required"));
   CourseSection s=sections.findById(sectionId).orElseThrow(()->SamsException.notFound("Course section not found"));
   boolean allowed=viewer.getRole()==UserRole.ADMIN||(viewer.getRole()==UserRole.FACULTY&&s.getFaculty().stream().anyMatch(f->f.getId().equals(viewer.getId())));
   if(!allowed)throw SamsException.forbidden("You are not assigned to this course section");
   return RosterResponse.of(s,enrolments.findByCourseSection_IdOrderByStudent_FullNameAsc(sectionId).stream().map(RosterResponse.RosterStudent::of).toList());
 }
}
