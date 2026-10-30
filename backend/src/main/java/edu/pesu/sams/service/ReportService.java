package edu.pesu.sams.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.opencsv.CSVWriter;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.exception.SamsException;
import edu.pesu.sams.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
 private final UserAccountRepository users; private final CourseSectionRepository sections; private final EnrollmentRepository enrolments;
 private final ClassSessionRepository sessions; private final AttendanceRecordRepository records; private final UserManagementService management; private final AttendanceCalculator calculator;
 public ReportService(UserAccountRepository users,CourseSectionRepository sections,EnrollmentRepository enrolments,ClassSessionRepository sessions,AttendanceRecordRepository records,UserManagementService management,AttendanceCalculator calculator){this.users=users;this.sections=sections;this.enrolments=enrolments;this.sessions=sessions;this.records=records;this.management=management;this.calculator=calculator;}
 @Transactional(readOnly=true) public List<Map<String,Object>> studentWise(Long sectionId,LocalDate from,LocalDate to,String email,UserRole role){
   validateRange(from,to);CourseSection section=management.requireSection(sectionId);authoriseSection(section,email,role);
   List<ClassSession> window=sessions.findByCourseSection_IdAndDateBetweenOrderByDateAscStartTimeAsc(sectionId,from,to).stream().filter(ClassSession::isSubmitted).toList();
   Set<Long> sessionIds=window.stream().map(ClassSession::getId).collect(Collectors.toSet());int held=window.size();List<Map<String,Object>> result=new ArrayList<>();
   for(Enrollment e:enrolments.findByCourseSection_IdOrderByStudent_FullNameAsc(sectionId)){
     List<AttendanceRecord> marks=records.findByStudent_Id(e.getStudent().getId()).stream().filter(r->sessionIds.contains(r.getSession().getId())).toList();long attended=marks.stream().filter(r->r.getStatus()==AttendanceStatus.PRESENT||r.getStatus()==AttendanceStatus.OD).count();BigDecimal pct=calculator.percentage(attended,held);
     result.add(Map.of("studentId",e.getStudent().getInstitutionId(),"name",e.getStudent().getFullName(),"email",e.getStudent().getEmail(),"held",held,"attended",attended,"percentage",pct));
   }
   return result;
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> defaulters(Long sectionId,LocalDate from,LocalDate to,String email,UserRole role,Integer requestedThreshold){
   int threshold=requestedThreshold==null?management.getThreshold():requestedThreshold;if(threshold<1||threshold>100)throw SamsException.badRequest("Threshold must be between 1 and 100");
   return studentWise(sectionId,from,to,email,role).stream().filter(r->((BigDecimal)r.get("percentage")).compareTo(BigDecimal.valueOf(threshold))<0).toList();
 }
 @Transactional(readOnly=true) public byte[] csv(List<Map<String,Object>> rows){
   StringWriter out=new StringWriter();try(CSVWriter writer=new CSVWriter(out)){writer.writeNext(new String[]{"Student ID","Name","Email","Sessions Held","Sessions Attended","Attendance Percentage"});for(Map<String,Object> r:rows){writer.writeNext(new String[]{safe(r.get("studentId")),safe(r.get("name")),safe(r.get("email")),String.valueOf(r.get("held")),String.valueOf(r.get("attended")),String.valueOf(r.get("percentage"))});}}catch(Exception ex){throw new IllegalStateException("Unable to produce CSV report");}return out.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
 }
 @Transactional(readOnly=true) public byte[] pdf(String heading,Long sectionId,LocalDate from,LocalDate to,List<Map<String,Object>> rows){
   try{ByteArrayOutputStream out=new ByteArrayOutputStream();Document d=new Document(PageSize.A4.rotate());PdfWriter.getInstance(d,out);d.open();d.add(new Paragraph("Student Attendance Management System"));d.add(new Paragraph(heading+" — Section #"+sectionId));d.add(new Paragraph("Date range: "+from+" to "+to));d.add(new Paragraph(" "));
     PdfPTable table=new PdfPTable(6);table.setWidthPercentage(100);for(String h:new String[]{"Student ID","Name","Email","Held","Attended","Percentage"})table.addCell(h);
     for(Map<String,Object> r:rows){table.addCell(safe(r.get("studentId")));table.addCell(safe(r.get("name")));table.addCell(safe(r.get("email")));table.addCell(String.valueOf(r.get("held")));table.addCell(String.valueOf(r.get("attended")));table.addCell(String.valueOf(r.get("percentage")));}d.add(table);d.close();return out.toByteArray();
   }catch(Exception ex){throw new IllegalStateException("Unable to produce PDF report");}
 }
 @Transactional(readOnly=true) public List<Map<String,Object>> sectionAttendanceForScheduled(Long sectionId,LocalDate from,LocalDate to){return studentWise(sectionId,from,to,"scheduler@sams.local",UserRole.ADMIN);}
 private void authoriseSection(CourseSection section,String email,UserRole role){if(role==UserRole.ADMIN)return;if(role==UserRole.FACULTY&&management.isAssigned(section,email))return;throw SamsException.forbidden("You are not permitted to view reports for this course section");}
 private void validateRange(LocalDate from,LocalDate to){if(from==null||to==null||to.isBefore(from))throw SamsException.badRequest("A valid from/to date range is required");if(to.isAfter(from.plusYears(2)))throw SamsException.badRequest("Report date range cannot exceed 2 years");}
 private String safe(Object value){String s=value==null?"":String.valueOf(value);if(!s.isEmpty()&&"=+-@".indexOf(s.charAt(0))>=0)return "'"+s;return s;}
}
