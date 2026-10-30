package edu.pesu.sams.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import edu.pesu.sams.dto.*;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.exception.SamsException;
import edu.pesu.sams.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserManagementService {
 private final UserAccountRepository users; private final CourseSectionRepository sections; private final EnrollmentRepository enrolments;
 private final PasswordEncoder encoder; private final PasswordPolicy passwordPolicy; private final AuditService audit; private final AppSettingRepository settings;
 public UserManagementService(UserAccountRepository users,CourseSectionRepository sections,EnrollmentRepository enrolments,PasswordEncoder encoder,PasswordPolicy passwordPolicy,AuditService audit,AppSettingRepository settings){this.users=users;this.sections=sections;this.enrolments=enrolments;this.encoder=encoder;this.passwordPolicy=passwordPolicy;this.audit=audit;this.settings=settings;}
 @Transactional(readOnly=true) public List<Map<String,Object>> listUsers(){return users.findAll().stream().map(this::userDto).toList();}
 @Transactional public Map<String,Object> createUser(CreateUserRequest r,String actor,HttpServletRequest req){
   String id=r.institutionId().trim(),email=r.email().trim().toLowerCase(Locale.ROOT);
   if(users.existsByInstitutionIdIgnoreCase(id))throw SamsException.conflict("SAMS-USR-409","institutionId already exists");
   if(users.existsByEmailIgnoreCase(email))throw SamsException.conflict("SAMS-USR-409","email already exists");
   passwordPolicy.validate(r.password());UserAccount u=new UserAccount();u.setInstitutionId(id);u.setFullName(r.fullName().trim());u.setEmail(email);u.setRole(r.role());u.setPasswordHash(encoder.encode(r.password()));u.setActive(true);users.save(u);
   audit.record(actor,"USER_CREATED","USER",id,null,u.getRole().name(),null,req);return userDto(u);
 }
 @Transactional public Map<String,Object> updateUser(Long id,UpdateUserRequest r,String actor,HttpServletRequest req){
   UserAccount u=users.findById(id).orElseThrow(()->SamsException.notFound("User not found"));String old=u.getFullName()+"|"+u.getEmail()+"|"+u.getRole()+"|"+u.isActive();
   String email=r.email().trim().toLowerCase(Locale.ROOT);if(!email.equalsIgnoreCase(u.getEmail())&&users.existsByEmailIgnoreCase(email))throw SamsException.conflict("SAMS-USR-409","email already exists");
   if(Boolean.FALSE.equals(r.active())&&u.getEmail().equalsIgnoreCase(actor))throw SamsException.badRequest("You cannot deactivate your own account");
   u.setFullName(r.fullName().trim());u.setEmail(email);u.setRole(r.role());if(r.active()!=null)u.setActive(r.active());users.save(u);
   audit.record(actor,"USER_UPDATED","USER",u.getInstitutionId(),old,u.getFullName()+"|"+u.getEmail()+"|"+u.getRole()+"|"+u.isActive(),null,req);return userDto(u);
 }
 @Transactional public Map<String,Object> deactivateUser(Long id,String actor,HttpServletRequest req){UserAccount u=users.findById(id).orElseThrow(()->SamsException.notFound("User not found"));if(u.getEmail().equalsIgnoreCase(actor))throw SamsException.badRequest("You cannot deactivate your own account");u.setActive(false);audit.record(actor,"USER_DEACTIVATED","USER",u.getInstitutionId(),"active","inactive",null,req);return userDto(u);}
 @Transactional public Map<String,Object> createSection(CourseSectionRequest r,String actor,HttpServletRequest req){
   CourseSection s=new CourseSection();s.setCourseCode(r.courseCode().trim().toUpperCase(Locale.ROOT));s.setCourseName(r.courseName().trim());s.setSemester(r.semester().trim());s.setSection(r.section().trim().toUpperCase(Locale.ROOT));s.setAcademicYear(r.academicYear().trim());
   Set<UserAccount> faculty=new HashSet<>();for(Long id:r.facultyIds()) {UserAccount u=users.findById(id).orElseThrow(()->SamsException.notFound("Faculty user not found: "+id));if(u.getRole()!=UserRole.FACULTY||!u.isActive())throw SamsException.badRequest("Every assigned user must be an active Faculty member");faculty.add(u);}s.setFaculty(faculty);sections.save(s);
   audit.record(actor,"COURSE_SECTION_CREATED","COURSE_SECTION",String.valueOf(s.getId()),null,s.getCourseCode()+"/"+s.getSection(),null,req);return sectionDto(s);
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> listSections(String email,UserRole role){
   List<CourseSection> list;
   if(role==UserRole.ADMIN)list=sections.findAll();
   else if(role==UserRole.FACULTY)list=sections.findDistinctByFaculty_EmailIgnoreCaseAndActiveTrue(email);
   else {UserAccount u=users.findByEmailIgnoreCase(email).orElseThrow(()->SamsException.notFound("User not found"));list=enrolments.findByStudent_Id(u.getId()).stream().map(Enrollment::getCourseSection).distinct().toList();}
   return list.stream().map(this::sectionDto).toList();
 }
 @Transactional public Map<String,Object> enrol(Long sectionId,EnrollmentRequest r,String actor,HttpServletRequest req){
   CourseSection s=sections.findById(sectionId).orElseThrow(()->SamsException.notFound("Course section not found"));UserAccount student=users.findByInstitutionIdIgnoreCase(r.institutionId().trim()).orElseThrow(()->SamsException.notFound("Student not found"));
   if(student.getRole()!=UserRole.STUDENT||!student.isActive())throw SamsException.badRequest("Only active student accounts can be enrolled");
   if(enrolments.existsByCourseSection_IdAndStudent_Id(sectionId,student.getId()))throw SamsException.conflict("SAMS-ENR-409","Student is already enrolled in this course section");
   Enrollment e=enrolments.save(new Enrollment(s,student));audit.record(actor,"STUDENT_ENROLLED","ENROLMENT",String.valueOf(e.getId()),null,student.getInstitutionId()+" -> "+s.getCourseCode(),null,req);return Map.of("id",e.getId(),"courseSectionId",sectionId,"studentId",student.getInstitutionId(),"studentName",student.getFullName());
 }
 @Transactional public Map<String,Object> importEnrolments(Long sectionId,MultipartFile file,String actor,HttpServletRequest req){
   if(file==null||file.isEmpty())throw SamsException.badRequest("CSV file is required");if(file.getSize()>2*1024*1024)throw new SamsException(HttpStatus.BAD_REQUEST,"SAMS-IMP-400","CSV file must be 2 MB or smaller");
   CourseSection s=sections.findById(sectionId).orElseThrow(()->SamsException.notFound("Course section not found"));int imported=0,row=0;List<Map<String,Object>> errors=new ArrayList<>();
   try(CSVReader reader=new CSVReader(new InputStreamReader(file.getInputStream(),StandardCharsets.UTF_8))){
     String[] cols=reader.readNext();if(cols==null)throw SamsException.badRequest("CSV is empty");int idIndex=-1;for(int i=0;i<cols.length;i++){if("institutionId".equalsIgnoreCase(cols[i].trim())||"studentId".equalsIgnoreCase(cols[i].trim())){idIndex=i;break;}}
     if(idIndex<0)throw SamsException.badRequest("CSV must have a header named institutionId or studentId");String[] cells;
     while((cells=reader.readNext())!=null){row++;if(row>1000)throw new SamsException(HttpStatus.BAD_REQUEST,"SAMS-IMP-400","CSV may contain at most 1,000 data rows");
       String instId=idIndex<cells.length?cells[idIndex].trim():"";String error=null;
       if(instId.isBlank())error="institutionId is blank";else {UserAccount student=users.findByInstitutionIdIgnoreCase(instId).orElse(null);if(student==null)error="student not found";else if(student.getRole()!=UserRole.STUDENT||!student.isActive())error="account is not an active student";else if(enrolments.existsByCourseSection_IdAndStudent_Id(sectionId,student.getId()))error="already enrolled";else {enrolments.save(new Enrollment(s,student));imported++;}}
       if(error!=null)errors.add(Map.of("row",row+1,"institutionId",instId,"reason",error));
     }
   }catch(IOException|CsvValidationException ex){throw SamsException.badRequest("Unable to read CSV file");}
   audit.record(actor,"ENROLMENT_CSV_IMPORTED","COURSE_SECTION",String.valueOf(sectionId),null,"imported="+imported+", rejected="+errors.size(),null,req);
   return Map.of("imported",imported,"rejected",errors.size(),"errors",errors);
 }
 @Transactional public Map<String,Object> updateThreshold(int threshold,String actor,HttpServletRequest req){if(threshold<1||threshold>100)throw SamsException.badRequest("Threshold must be between 1 and 100");AppSetting setting=settings.findByKey("attendance.threshold").orElseGet(()->settings.save(new AppSetting("attendance.threshold","75")));String old=setting.getValue();setting.setValue(String.valueOf(threshold));settings.save(setting);audit.record(actor,"ATTENDANCE_THRESHOLD_UPDATED","SETTING","attendance.threshold",old,String.valueOf(threshold),null,req);return Map.of("threshold",threshold);}
 @Transactional(readOnly=true) public int getThreshold(){return settings.findByKey("attendance.threshold").map(s->{try{return Integer.parseInt(s.getValue());}catch(NumberFormatException e){return 75;}}).orElse(75);}
 @Transactional(readOnly=true) public UserAccount requireUser(String email){return users.findByEmailIgnoreCase(email).orElseThrow(()->SamsException.notFound("User not found"));}
 @Transactional(readOnly=true) public CourseSection requireSection(Long id){return sections.findById(id).orElseThrow(()->SamsException.notFound("Course section not found"));}
 @Transactional(readOnly=true) public boolean isAssigned(CourseSection section,String email){return section.getFaculty().stream().anyMatch(u->u.getEmail().equalsIgnoreCase(email));}
 private Map<String,Object> userDto(UserAccount u){return Map.of("id",u.getId(),"institutionId",u.getInstitutionId(),"fullName",u.getFullName(),"email",u.getEmail(),"role",u.getRole().name(),"active",u.isActive(),"createdAt",String.valueOf(u.getCreatedAt()));}
 private Map<String,Object> sectionDto(CourseSection s){return Map.of("id",s.getId(),"courseCode",s.getCourseCode(),"courseName",s.getCourseName(),"semester",s.getSemester(),"section",s.getSection(),"academicYear",s.getAcademicYear(),"active",s.isActive(),"faculty",s.getFaculty().stream().map(u->Map.of("id",u.getId(),"institutionId",u.getInstitutionId(),"fullName",u.getFullName())).toList());}
}
