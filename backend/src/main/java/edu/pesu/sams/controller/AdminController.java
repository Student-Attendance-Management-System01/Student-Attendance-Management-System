package edu.pesu.sams.controller;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.repository.*;
import edu.pesu.sams.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController @RequestMapping("/api/v1")
public class AdminController {
 private final UserManagementService management; private final UserAccountRepository users; private final CourseSectionRepository sections;
 private final EnrollmentRepository enrolments; private final NotificationOutboxRepository outbox; private final ReportLogRepository reportLogs;
 public AdminController(UserManagementService management,UserAccountRepository users,CourseSectionRepository sections,EnrollmentRepository enrolments,NotificationOutboxRepository outbox,ReportLogRepository reportLogs){this.management=management;this.users=users;this.sections=sections;this.enrolments=enrolments;this.outbox=outbox;this.reportLogs=reportLogs;}
 @GetMapping("/admin/users") public List<Map<String,Object>> users(){return management.listUsers();}
 @PostMapping("/admin/users") public ResponseEntity<Map<String,Object>> createUser(@Valid @RequestBody CreateUserRequest request,Authentication auth,HttpServletRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(management.createUser(request,auth.getName(),req));}
 @PutMapping("/admin/users/{id}") public Map<String,Object> updateUser(@PathVariable Long id,@Valid @RequestBody UpdateUserRequest request,Authentication auth,HttpServletRequest req){return management.updateUser(id,request,auth.getName(),req);}
 @PatchMapping("/admin/users/{id}/deactivate") public Map<String,Object> deactivate(@PathVariable Long id,Authentication auth,HttpServletRequest req){return management.deactivateUser(id,auth.getName(),req);}
 @GetMapping("/admin/faculty") public List<Map<String,Object>> faculty(){return users.findByRoleAndActiveTrue(UserRole.FACULTY).stream().map(u->Map.<String,Object>of("id",u.getId(),"institutionId",u.getInstitutionId(),"fullName",u.getFullName(),"email",u.getEmail())).toList();}
 @GetMapping("/admin/course-sections") public List<Map<String,Object>> adminSections(){return management.listSections("",UserRole.ADMIN);}
 @GetMapping("/course-sections") public List<Map<String,Object>> accessibleSections(Authentication auth){UserAccount u=management.requireUser(auth.getName());return management.listSections(auth.getName(),u.getRole());}
 @PostMapping("/admin/course-sections/{id}/enrolments") public ResponseEntity<Map<String,Object>> enrol(@PathVariable Long id,@Valid @RequestBody EnrollmentRequest request,Authentication auth,HttpServletRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(management.enrol(id,request,auth.getName(),req));}
 @PostMapping(value="/admin/course-sections/{id}/enrolments/import", consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public Map<String,Object> importEnrolments(@PathVariable Long id,@RequestPart("file") MultipartFile file,Authentication auth,HttpServletRequest req){return management.importEnrolments(id,file,auth.getName(),req);}
 @GetMapping("/admin/course-sections/{id}/students") public List<Map<String,Object>> sectionStudents(@PathVariable Long id){return enrolments.findByCourseSection_IdOrderByStudent_FullNameAsc(id).stream().map(e->Map.<String,Object>of("id",e.getStudent().getId(),"institutionId",e.getStudent().getInstitutionId(),"fullName",e.getStudent().getFullName(),"email",e.getStudent().getEmail())).toList();}
 @GetMapping("/admin/settings/threshold") public Map<String,Integer> getThreshold(){return Map.of("threshold",management.getThreshold());}
 @PutMapping("/admin/settings/threshold") public Map<String,Object> threshold(@RequestBody Map<String,Integer> body,Authentication auth,HttpServletRequest req){Integer value=body.get("threshold");if(value==null)throw edu.pesu.sams.exception.SamsException.badRequest("threshold is required");return management.updateThreshold(value,auth.getName(),req);}
 @GetMapping("/admin/notifications") public Map<String,Object> notificationStatus(){return Map.of("pending",outbox.countByStatus(OutboxStatus.PENDING),"delivered",outbox.countByStatus(OutboxStatus.DELIVERED),"failed",outbox.countByStatus(OutboxStatus.FAILED));}
 @GetMapping("/admin/report-logs") public List<Map<String,Object>> reportLogs(){return reportLogs.findAll().stream().sorted(Comparator.comparing(ReportLog::getCreatedAt).reversed()).limit(100).map(r->Map.<String,Object>of("id",r.getId(),"reportType",r.getReportType(),"recipientEmail",r.getRecipientEmail()==null?"":r.getRecipientEmail(),"status",r.getStatus(),"details",r.getDetails()==null?"":r.getDetails(),"createdAt",r.getCreatedAt().toString())).toList();}
}
