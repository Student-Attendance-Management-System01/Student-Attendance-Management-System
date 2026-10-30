package edu.pesu.sams.service;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.exception.SamsException;
import edu.pesu.sams.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Faculty profiles (SAMS-F-010, F-011): accounts with role FACULTY, identified by their Employee ID. */
@Service
public class FacultyService {
 private final UserAccountRepository users; private final CourseSectionRepository sections; private final PasswordEncoder encoder; private final PasswordPolicy passwordPolicy; private final AuditService audit;
 public FacultyService(UserAccountRepository users,CourseSectionRepository sections,PasswordEncoder encoder,PasswordPolicy passwordPolicy,AuditService audit){this.users=users;this.sections=sections;this.encoder=encoder;this.passwordPolicy=passwordPolicy;this.audit=audit;}
 @Transactional(readOnly=true) public FacultyResponse get(String institutionId){return response(requireFaculty(institutionId));}
 @Transactional public FacultyResponse create(CreateFacultyRequest r,String actor,HttpServletRequest req){
   String id=r.institutionId().trim().toUpperCase(Locale.ROOT),email=r.email().trim().toLowerCase(Locale.ROOT);
   if(users.existsByInstitutionIdIgnoreCase(id))throw SamsException.conflict("SAMS-USR-409","institutionId already exists");
   if(users.existsByEmailIgnoreCase(email))throw SamsException.conflict("SAMS-USR-409","email already exists");
   passwordPolicy.validate(r.password());
   UserAccount u=new UserAccount();u.setInstitutionId(id);u.setFullName(r.fullName().trim());u.setEmail(email);u.setRole(UserRole.FACULTY);u.setPasswordHash(encoder.encode(r.password()));u.setActive(true);users.save(u);
   audit.record(actor,"FACULTY_CREATED","USER",id,null,u.getFullName()+"|"+u.getEmail(),null,req);return response(u);
 }
 @Transactional public FacultyResponse update(String institutionId,UpdateFacultyRequest r,String actor,HttpServletRequest req){
   UserAccount u=requireFaculty(institutionId);String old=u.getFullName()+"|"+u.getEmail()+"|"+u.isActive();
   String email=r.email().trim().toLowerCase(Locale.ROOT);
   if(!email.equalsIgnoreCase(u.getEmail())&&users.existsByEmailIgnoreCase(email))throw SamsException.conflict("SAMS-USR-409","email already exists");
   if(Boolean.FALSE.equals(r.active())&&u.isActive()){
     // BR-7: an active course-section must keep at least one active Faculty member.
     for(CourseSection s:sections.findDistinctByFaculty_IdOrderByCourseCodeAsc(u.getId()))
       if(s.isActive()&&s.getFaculty().stream().noneMatch(f->!f.getId().equals(u.getId())&&f.isActive()))
         throw SamsException.conflict("SAMS-FAC-409","Assign another faculty member to "+s.getCourseCode()+" section "+s.getSection()+" before deactivating this account");
   }
   u.setFullName(r.fullName().trim());u.setEmail(email);if(r.active()!=null)u.setActive(r.active());users.save(u);
   audit.record(actor,"FACULTY_UPDATED","USER",u.getInstitutionId(),old,u.getFullName()+"|"+u.getEmail()+"|"+u.isActive(),null,req);return response(u);
 }
 private FacultyResponse response(UserAccount u){
   List<FacultyResponse.SectionSummary> assigned=u.getId()==null?List.of():sections.findDistinctByFaculty_IdOrderByCourseCodeAsc(u.getId()).stream()
     .map(s->new FacultyResponse.SectionSummary(s.getId(),s.getCourseCode(),s.getSection(),s.getSemester(),s.getAcademicYear())).toList();
   return FacultyResponse.of(u,assigned);
 }
 private UserAccount requireFaculty(String institutionId){
   return users.findByInstitutionIdIgnoreCase(institutionId.trim()).filter(u->u.getRole()==UserRole.FACULTY).orElseThrow(()->SamsException.notFound("Faculty member not found"));
 }
}
