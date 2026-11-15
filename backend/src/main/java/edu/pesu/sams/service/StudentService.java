package edu.pesu.sams.service;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.exception.SamsException;
import edu.pesu.sams.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

/** Student records (SAMS-F-010, F-011): accounts with role STUDENT, identified by their institution ID (USN). */
@Service
public class StudentService {
 public static final int MAX_PAGE_SIZE=100;
 private final UserAccountRepository users; private final PasswordEncoder encoder; private final PasswordPolicy passwordPolicy; private final AuditService audit;
 public StudentService(UserAccountRepository users,PasswordEncoder encoder,PasswordPolicy passwordPolicy,AuditService audit){this.users=users;this.encoder=encoder;this.passwordPolicy=passwordPolicy;this.audit=audit;}
 @Transactional(readOnly=true) public PageResponse<StudentResponse> list(String search,Boolean active,int page,int size){
   if(page<0)throw SamsException.badRequest("page must be 0 or greater");
   if(size<1||size>MAX_PAGE_SIZE)throw SamsException.badRequest("size must be between 1 and "+MAX_PAGE_SIZE);
   String term=search==null?"":search.trim().toLowerCase(Locale.ROOT).replace("\\","\\\\").replace("%","\\%").replace("_","\\_");
   Page<UserAccount> result=users.search(UserRole.STUDENT,active,"%"+term+"%",PageRequest.of(page,size,Sort.by("institutionId")));
   return PageResponse.of(result,StudentResponse::of);
 }
 @Transactional(readOnly=true) public StudentResponse get(String institutionId){return StudentResponse.of(requireStudent(institutionId));}
 @Transactional(readOnly=true) public StudentResponse own(String email){
   UserAccount u=users.findByEmailIgnoreCase(email).filter(a->a.getRole()==UserRole.STUDENT).orElseThrow(()->SamsException.forbidden("Only students have a student profile"));
   return StudentResponse.of(u);
 }
 @Transactional public StudentResponse create(CreateStudentRequest r,String actor,HttpServletRequest req){
   String id=r.institutionId().trim().toUpperCase(Locale.ROOT),email=r.email().trim().toLowerCase(Locale.ROOT);
   if(users.existsByInstitutionIdIgnoreCase(id))throw SamsException.conflict("SAMS-USR-409","institutionId already exists");
   if(users.existsByEmailIgnoreCase(email))throw SamsException.conflict("SAMS-USR-409","email already exists");
   passwordPolicy.validate(r.password());
   UserAccount u=new UserAccount();u.setInstitutionId(id);u.setFullName(r.fullName().trim());u.setEmail(email);u.setRole(UserRole.STUDENT);u.setPasswordHash(encoder.encode(r.password()));u.setActive(true);users.save(u);
   audit.record(actor,"STUDENT_CREATED","USER",id,null,u.getFullName()+"|"+u.getEmail(),null,req);return StudentResponse.of(u);
 }
 @Transactional public StudentResponse update(String institutionId,UpdateStudentRequest r,String actor,HttpServletRequest req){
   UserAccount u=requireStudent(institutionId);String old=u.getFullName()+"|"+u.getEmail()+"|"+u.isActive();
   String email=r.email().trim().toLowerCase(Locale.ROOT);
   if(!email.equalsIgnoreCase(u.getEmail())&&users.existsByEmailIgnoreCase(email))throw SamsException.conflict("SAMS-USR-409","email already exists");
   u.setFullName(r.fullName().trim());u.setEmail(email);if(r.active()!=null)u.setActive(r.active());users.save(u);
   audit.record(actor,"STUDENT_UPDATED","USER",u.getInstitutionId(),old,u.getFullName()+"|"+u.getEmail()+"|"+u.isActive(),null,req);return StudentResponse.of(u);
 }
 private UserAccount requireStudent(String institutionId){
   return users.findByInstitutionIdIgnoreCase(institutionId.trim()).filter(u->u.getRole()==UserRole.STUDENT).orElseThrow(()->SamsException.notFound("Student not found"));
 }
}
