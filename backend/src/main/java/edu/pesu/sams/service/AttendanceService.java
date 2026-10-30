package edu.pesu.sams.service;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.exception.SamsException;
import edu.pesu.sams.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AttendanceService {
 private final UserAccountRepository users; private final CourseSectionRepository sections; private final EnrollmentRepository enrolments;
 private final ClassSessionRepository sessions; private final AttendanceRecordRepository records; private final UserManagementService management;
 private final AuditService audit; private final NotificationService notifications; private final Clock clock; private final AttendanceCalculator calculator;
 public AttendanceService(UserAccountRepository users,CourseSectionRepository sections,EnrollmentRepository enrolments,ClassSessionRepository sessions,AttendanceRecordRepository records,UserManagementService management,AuditService audit,NotificationService notifications,Clock clock,AttendanceCalculator calculator){this.users=users;this.sections=sections;this.enrolments=enrolments;this.sessions=sessions;this.records=records;this.management=management;this.audit=audit;this.notifications=notifications;this.clock=clock;this.calculator=calculator;}

 @Transactional public Map<String,Object> createSession(String email,CreateSessionRequest r,HttpServletRequest req){
   UserAccount faculty=management.requireUser(email);CourseSection section=management.requireSection(r.courseSectionId());requireFacultyAssigned(section,email);
   if(!section.isActive())throw SamsException.badRequest("Cannot create sessions for an inactive course section");
   if(!r.endTime().isAfter(r.startTime()))throw SamsException.badRequest("Session end time must be after start time");
   for(ClassSession existing:sessions.findByCourseSection_IdOrderByDateDescStartTimeDesc(section.getId())){
     if(existing.getDate().equals(r.date())&&r.startTime().isBefore(existing.getEndTime())&&r.endTime().isAfter(existing.getStartTime()))
       throw SamsException.conflict("SAMS-SES-409","This session overlaps another session for the same course section");
   }
   ClassSession session=new ClassSession();session.setCourseSection(section);session.setDate(r.date());session.setStartTime(r.startTime());session.setEndTime(r.endTime());session.setType(r.type());session.setCreatedBy(faculty);sessions.save(session);
   audit.record(email,"CLASS_SESSION_CREATED","CLASS_SESSION",String.valueOf(session.getId()),null,section.getCourseCode()+" "+r.date(),null,req);return sessionDto(session);
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> listSessions(String email,UserRole role){
   List<ClassSession> found;
   if(role==UserRole.ADMIN)found=sessions.findAll().stream().sorted(Comparator.comparing(ClassSession::getDate).reversed().thenComparing(ClassSession::getStartTime)).toList();
   else if(role==UserRole.FACULTY){Set<Long> ids=management.listSections(email,role).stream().map(m->Long.valueOf(m.get("id").toString())).collect(Collectors.toSet());found=sessions.findAll().stream().filter(s->ids.contains(s.getCourseSection().getId())).sorted(Comparator.comparing(ClassSession::getDate).reversed().thenComparing(ClassSession::getStartTime)).toList();}
   else {UserAccount student=management.requireUser(email);Set<Long> ids=enrolments.findByStudent_Id(student.getId()).stream().map(e->e.getCourseSection().getId()).collect(Collectors.toSet());found=sessions.findAll().stream().filter(s->ids.contains(s.getCourseSection().getId())&&s.isSubmitted()).sorted(Comparator.comparing(ClassSession::getDate).reversed()).toList();}
   return found.stream().map(this::sessionDto).toList();
 }
 @Transactional(readOnly=true) public Map<String,Object> roster(Long sessionId,String email){
   ClassSession session=requireSession(sessionId);requireFacultyAssigned(session.getCourseSection(),email);
   Map<Long,AttendanceRecord> marked=records.findBySession_Id(sessionId).stream().collect(Collectors.toMap(r->r.getStudent().getId(),Function.identity()));
   List<Map<String,Object>> students=enrolments.findByCourseSection_IdOrderByStudent_FullNameAsc(session.getCourseSection().getId()).stream().map(e->{UserAccount s=e.getStudent();AttendanceRecord rec=marked.get(s.getId());return Map.<String,Object>of("studentId",s.getInstitutionId(),"name",s.getFullName(),"email",s.getEmail(),"status",rec==null?"UNMARKED":rec.getStatus().name(),"attendanceRecordId",rec==null?0L:rec.getId());}).toList();
   return Map.of("session",sessionDto(session),"submitted",session.isSubmitted(),"students",students);
 }
 @Transactional public Map<String,Object> submitAttendance(Long sessionId,AttendanceSubmissionRequest request,String email,HttpServletRequest req){
   ClassSession session=requireSession(sessionId);requireFacultyAssigned(session.getCourseSection(),email);
   if(session.isSubmitted())throw SamsException.conflict("SAMS-ATT-409","Attendance has already been submitted for this session");
   List<Enrollment> enrolled=enrolments.findByCourseSection_IdOrderByStudent_FullNameAsc(session.getCourseSection().getId());
   Map<String,UserAccount> expected=enrolled.stream().collect(Collectors.toMap(e->e.getStudent().getInstitutionId().toUpperCase(Locale.ROOT),Enrollment::getStudent));
   Map<String,AttendanceStatus> submitted=new HashMap<>();
   for(AttendanceSubmissionRequest.AttendanceMark mark:request.records()){
     String key=mark.studentId().trim().toUpperCase(Locale.ROOT);if(mark.status()==null)throw SamsException.badRequest("Every student must have a status before submitting");
     if(!expected.containsKey(key))throw new SamsException(HttpStatus.BAD_REQUEST,"SAMS-ATT-400","Roster includes a student who is not enrolled in this course section");
     if(submitted.putIfAbsent(key,mark.status())!=null)throw new SamsException(HttpStatus.BAD_REQUEST,"SAMS-ATT-400","Roster contains a duplicate student");
   }
   if(submitted.size()!=expected.size())throw new SamsException(HttpStatus.BAD_REQUEST,"SAMS-ATT-400","Every enrolled student must be marked before submitting attendance");
   List<AttendanceRecord> save=new ArrayList<>();Map<Long,Boolean> wasBelow=new HashMap<>();int present=0,absent=0,od=0;int threshold=management.getThreshold();
   for(var entry:expected.entrySet()){
     AttendanceStatus status=submitted.get(entry.getKey());UserAccount student=entry.getValue();long priorHeld=records.countByStudent_IdAndSession_CourseSection_Id(student.getId(),session.getCourseSection().getId());long priorAttended=records.countByStudent_IdAndSession_CourseSection_IdAndStatusIn(student.getId(),session.getCourseSection().getId(),List.of(AttendanceStatus.PRESENT,AttendanceStatus.OD));BigDecimal priorPct=calculator.percentage(priorAttended,priorHeld);wasBelow.put(student.getId(),priorHeld>0&&priorPct.compareTo(BigDecimal.valueOf(threshold))<0);save.add(new AttendanceRecord(session,student,status));
     if(status==AttendanceStatus.PRESENT)present++;else if(status==AttendanceStatus.ABSENT)absent++;else if(status==AttendanceStatus.OD)od++;
   }
   records.saveAll(save);session.setSubmitted(true);sessions.save(session);
   audit.record(email,"ATTENDANCE_SUBMITTED","CLASS_SESSION",String.valueOf(sessionId),null,"records="+save.size(),null,req);
   for(UserAccount student:expected.values())notifyIfBelowThreshold(student,session.getCourseSection(),wasBelow.getOrDefault(student.getId(),false));
   return Map.of("sessionId",sessionId,"present",present,"absent",absent,"od",od,"submittedAt",LocalDateTime.now(clock).toString());
 }
 @Transactional public Map<String,Object> editAttendance(Long sessionId,String studentId,EditAttendanceRequest request,String email,UserRole role,HttpServletRequest req){
   ClassSession session=requireSession(sessionId);UserAccount actor=management.requireUser(email);UserAccount student=users.findByInstitutionIdIgnoreCase(studentId).orElseThrow(()->SamsException.notFound("Student not found"));
   if(role!=UserRole.ADMIN)requireFacultyAssigned(session.getCourseSection(),email);
   if(role!=UserRole.ADMIN){LocalDateTime end=LocalDateTime.of(session.getDate(),session.getEndTime());if(LocalDateTime.now(clock).isAfter(end.plusHours(24)))throw new SamsException(HttpStatus.FORBIDDEN,"SAMS-ATT-403","The 24-hour attendance edit window has expired; only an Administrator may edit this record");}
   AttendanceRecord record=records.findBySession_IdAndStudent_Id(sessionId,student.getId()).orElseThrow(()->SamsException.notFound("Attendance record not found"));
   AttendanceStatus old=record.getStatus();long heldBefore=records.countByStudent_IdAndSession_CourseSection_Id(student.getId(),session.getCourseSection().getId());long attendedBefore=records.countByStudent_IdAndSession_CourseSection_IdAndStatusIn(student.getId(),session.getCourseSection().getId(),List.of(AttendanceStatus.PRESENT,AttendanceStatus.OD));boolean wasBelow=heldBefore>0&&calculator.percentage(attendedBefore,heldBefore).compareTo(BigDecimal.valueOf(management.getThreshold()))<0;record.setStatus(request.status());records.save(record);
   audit.record(email,"ATTENDANCE_EDITED","ATTENDANCE_RECORD",String.valueOf(record.getId()),old.name(),request.status().name(),request.reason(),req);
   notifyIfBelowThreshold(student,session.getCourseSection(),wasBelow);
   return Map.of("sessionId",sessionId,"studentId",student.getInstitutionId(),"oldStatus",old.name(),"newStatus",request.status().name(),"reason",request.reason());
 }
 @Transactional(readOnly=true) public Map<String,Object> ownSummary(String studentInstitutionId,String callerEmail){
   UserAccount student=users.findByInstitutionIdIgnoreCase(studentInstitutionId).orElseThrow(()->SamsException.notFound("Student not found"));
   if(!student.getEmail().equalsIgnoreCase(callerEmail))throw SamsException.forbidden("Students may view only their own attendance");
   int threshold=management.getThreshold();List<Map<String,Object>> courseRows=new ArrayList<>();
   for(Enrollment e:enrolments.findByStudent_Id(student.getId())){CourseSection section=e.getCourseSection();long held=records.countByStudent_IdAndSession_CourseSection_Id(student.getId(),section.getId());long attended=records.countByStudent_IdAndSession_CourseSection_IdAndStatusIn(student.getId(),section.getId(),List.of(AttendanceStatus.PRESENT,AttendanceStatus.OD));BigDecimal pct=calculator.percentage(attended,held);
     List<Map<String,Object>> sessionRows=records.findByStudent_Id(student.getId()).stream().filter(r->r.getSession().getCourseSection().getId().equals(section.getId())).sorted(Comparator.comparing((AttendanceRecord r)->r.getSession().getDate()).reversed()).map(r->Map.<String,Object>of("sessionId",r.getSession().getId(),"date",r.getSession().getDate().toString(),"type",r.getSession().getType().name(),"status",r.getStatus().name(),"attendanceRecordId",r.getId())).toList();
     courseRows.add(Map.of("courseSectionId",section.getId(),"courseCode",section.getCourseCode(),"courseName",section.getCourseName(),"section",section.getSection(),"held",held,"attended",attended,"percentage",pct,"belowThreshold",held>0&&pct.compareTo(BigDecimal.valueOf(threshold))<0,"sessions",sessionRows));
   }
   return Map.of("studentId",student.getInstitutionId(),"fullName",student.getFullName(),"threshold",threshold,"courses",courseRows);
 }
 @Transactional(readOnly=true) public Map<String,Object> sectionAttendance(Long sectionId,String email,UserRole role){
   CourseSection section=management.requireSection(sectionId);if(role!=UserRole.ADMIN)requireFacultyAssigned(section,email);
   List<ClassSession> sectionSessions=sessions.findByCourseSection_IdOrderByDateDescStartTimeDesc(sectionId);
   List<Map<String,Object>> sessionRows=sectionSessions.stream().map(s->{List<Map<String,Object>> studentRows=records.findBySession_Id(s.getId()).stream().map(r->Map.<String,Object>of("studentId",r.getStudent().getInstitutionId(),"name",r.getStudent().getFullName(),"status",r.getStatus().name())).toList();return Map.<String,Object>of("session",sessionDto(s),"records",studentRows);}).toList();
   return Map.of("courseSection",Map.of("id",section.getId(),"courseCode",section.getCourseCode(),"courseName",section.getCourseName(),"section",section.getSection()),"sessions",sessionRows);
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> enrolledStudentData(Long sectionId){return enrolments.findByCourseSection_IdOrderByStudent_FullNameAsc(sectionId).stream().map(e->Map.<String,Object>of("id",e.getStudent().getId(),"institutionId",e.getStudent().getInstitutionId(),"fullName",e.getStudent().getFullName(),"email",e.getStudent().getEmail())).toList();}
 private void notifyIfBelowThreshold(UserAccount student,CourseSection section,boolean wasBelow){
   long held=records.countByStudent_IdAndSession_CourseSection_Id(student.getId(),section.getId());if(held==0)return;
   long attended=records.countByStudent_IdAndSession_CourseSection_IdAndStatusIn(student.getId(),section.getId(),List.of(AttendanceStatus.PRESENT,AttendanceStatus.OD));BigDecimal pct=calculator.percentage(attended,held);
   if(!wasBelow&&pct.compareTo(BigDecimal.valueOf(management.getThreshold()))<0)notifications.enqueue(student.getEmail(),"SAMS low attendance alert", "Your attendance in "+section.getCourseCode()+" - "+section.getCourseName()+" is currently "+pct+"% (threshold: "+management.getThreshold()+"%). Please contact your faculty if you believe this is incorrect.");
 }
 private ClassSession requireSession(Long id){return sessions.findById(id).orElseThrow(()->SamsException.notFound("Class session not found"));}
 private void requireFacultyAssigned(CourseSection section,String email){if(!management.isAssigned(section,email))throw SamsException.forbidden("You are not assigned to this course section");}
 private Map<String,Object> sessionDto(ClassSession s){return Map.of("id",s.getId(),"courseSectionId",s.getCourseSection().getId(),"courseCode",s.getCourseSection().getCourseCode(),"courseName",s.getCourseSection().getCourseName(),"section",s.getCourseSection().getSection(),"date",s.getDate().toString(),"startTime",s.getStartTime().toString(),"endTime",s.getEndTime().toString(),"type",s.getType().name(),"submitted",s.isSubmitted());}
}
