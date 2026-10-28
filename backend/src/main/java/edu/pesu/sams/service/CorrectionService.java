package edu.pesu.sams.service;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.exception.SamsException;
import edu.pesu.sams.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;

@Service
public class CorrectionService {
 private final CorrectionRequestRepository corrections; private final AttendanceRecordRepository attendance; private final UserAccountRepository users;
 private final UserManagementService management; private final NotificationService notifications; private final AuditService audit; private final Clock clock; private final AttendanceCalculator calculator;
 public CorrectionService(CorrectionRequestRepository corrections,AttendanceRecordRepository attendance,UserAccountRepository users,UserManagementService management,NotificationService notifications,AuditService audit,Clock clock,AttendanceCalculator calculator){this.corrections=corrections;this.attendance=attendance;this.users=users;this.management=management;this.notifications=notifications;this.audit=audit;this.clock=clock;this.calculator=calculator;}
 @Transactional public Map<String,Object> create(CorrectionRequestCreate request,String email,HttpServletRequest http){
   UserAccount student=management.requireUser(email);AttendanceRecord record=attendance.findById(request.attendanceRecordId()).orElseThrow(()->SamsException.notFound("Attendance record not found"));
   if(!record.getStudent().getId().equals(student.getId()))throw SamsException.forbidden("You can request a correction only for your own attendance");
   if(record.getSession().getDate().isBefore(LocalDate.now(clock).minusDays(7)))throw new SamsException(org.springframework.http.HttpStatus.BAD_REQUEST,"SAMS-COR-400","Correction requests must be raised within 7 days of the session date");
   if(request.reason().trim().isEmpty())throw SamsException.badRequest("A reason is required");
   CorrectionRequest entity=corrections.save(new CorrectionRequest(record,student,request.requestedStatus(),request.reason().trim()));
   record.getSession().getCourseSection().getFaculty().forEach(f->notifications.enqueue(f.getEmail(),"SAMS attendance correction request",student.getFullName()+" ("+student.getInstitutionId()+") requested a correction for "+record.getSession().getCourseSection().getCourseCode()+" on "+record.getSession().getDate()+". Requested status: "+request.requestedStatus()+". Reason: "+request.reason()));
   audit.record(email,"CORRECTION_REQUESTED","CORRECTION_REQUEST",String.valueOf(entity.getId()),null,request.requestedStatus().name(),request.reason(),http);return dto(entity);
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> mine(String email){return corrections.findByStudent_EmailIgnoreCaseOrderByRequestedAtDesc(email).stream().map(this::dto).toList();}
 @Transactional(readOnly=true) public List<Map<String,Object>> pending(String email,UserRole role){
   List<CorrectionRequest> all=corrections.findByStatusOrderByRequestedAtAsc(CorrectionStatus.PENDING);
   return all.stream().filter(c->role==UserRole.ADMIN||management.isAssigned(c.getAttendanceRecord().getSession().getCourseSection(),email)).map(this::dto).toList();
 }
 @Transactional public Map<String,Object> decide(Long id,CorrectionDecisionRequest request,String email,HttpServletRequest http){
   if(request.status()!=CorrectionStatus.APPROVED&&request.status()!=CorrectionStatus.REJECTED)throw SamsException.badRequest("Decision must be APPROVED or REJECTED");
   CorrectionRequest correction=corrections.findById(id).orElseThrow(()->SamsException.notFound("Correction request not found"));
   if(!management.isAssigned(correction.getAttendanceRecord().getSession().getCourseSection(),email))throw SamsException.forbidden("You are not assigned to this course section");
   if(correction.getStatus()!=CorrectionStatus.PENDING)throw SamsException.conflict("SAMS-COR-409","This correction request has already been decided");
   AttendanceRecord record=correction.getAttendanceRecord();AttendanceStatus old=record.getStatus();
   if(request.status()==CorrectionStatus.APPROVED){long held=attendance.countByStudent_IdAndSession_CourseSection_Id(record.getStudent().getId(),record.getSession().getCourseSection().getId());long attended=attendance.countByStudent_IdAndSession_CourseSection_IdAndStatusIn(record.getStudent().getId(),record.getSession().getCourseSection().getId(),List.of(AttendanceStatus.PRESENT,AttendanceStatus.OD));int threshold=management.getThreshold();boolean wasBelow=held>0&&calculator.percentage(attended,held).compareTo(BigDecimal.valueOf(threshold))<0;long adjusted=attended-(isAttended(old)?1:0)+(isAttended(correction.getRequestedStatus())?1:0);record.setStatus(correction.getRequestedStatus());attendance.save(record);BigDecimal newPct=calculator.percentage(adjusted,held);if(!wasBelow&&held>0&&newPct.compareTo(BigDecimal.valueOf(threshold))<0)notifications.enqueue(record.getStudent().getEmail(),"SAMS low attendance alert","Your attendance in "+record.getSession().getCourseSection().getCourseCode()+" is now "+newPct+"% (threshold: "+threshold+"%).");audit.record(email,"ATTENDANCE_CORRECTED","ATTENDANCE_RECORD",String.valueOf(record.getId()),old.name(),record.getStatus().name(),"Correction request #"+id+": "+correction.getReason(),http);}
   correction.setStatus(request.status());correction.setDecidedBy(management.requireUser(email));correction.setDecidedAt(LocalDateTime.now(clock));correction.setDecisionNote(request.decisionNote());corrections.save(correction);
   String text="Your attendance correction request #"+id+" for "+record.getSession().getCourseSection().getCourseCode()+" was "+request.status().name().toLowerCase(Locale.ROOT)+"."+(request.decisionNote()==null?"":" Note: "+request.decisionNote());notifications.enqueue(correction.getStudent().getEmail(),"SAMS correction request decision",text);
   audit.record(email,"CORRECTION_DECIDED","CORRECTION_REQUEST",String.valueOf(id),CorrectionStatus.PENDING.name(),request.status().name(),request.decisionNote(),http);return dto(correction);
 }
 private boolean isAttended(AttendanceStatus status){return status==AttendanceStatus.PRESENT||status==AttendanceStatus.OD;}
 private Map<String,Object> dto(CorrectionRequest c){AttendanceRecord r=c.getAttendanceRecord();Map<String,Object> m=new LinkedHashMap<>();m.put("id",c.getId());m.put("attendanceRecordId",r.getId());m.put("studentId",c.getStudent().getInstitutionId());m.put("studentName",c.getStudent().getFullName());m.put("courseSectionId",r.getSession().getCourseSection().getId());m.put("courseCode",r.getSession().getCourseSection().getCourseCode());m.put("sessionDate",r.getSession().getDate().toString());m.put("currentStatus",r.getStatus().name());m.put("requestedStatus",c.getRequestedStatus().name());m.put("reason",c.getReason());m.put("status",c.getStatus().name());m.put("requestedAt",c.getRequestedAt().toString());m.put("decisionNote",c.getDecisionNote()==null?"":c.getDecisionNote());return m;}
}
