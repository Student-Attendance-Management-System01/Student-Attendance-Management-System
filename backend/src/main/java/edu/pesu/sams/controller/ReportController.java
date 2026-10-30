package edu.pesu.sams.controller;

import edu.pesu.sams.entity.UserAccount;
import edu.pesu.sams.service.ReportService;
import edu.pesu.sams.service.UserManagementService;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/v1/reports") @PreAuthorize("hasAnyRole('FACULTY','ADMIN')")
public class ReportController {
 private final ReportService reports; private final UserManagementService management;
 public ReportController(ReportService reports,UserManagementService management){this.reports=reports;this.management=management;}
 @GetMapping("/student-wise") public ResponseEntity<?> studentWise(@RequestParam Long courseSectionId,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(defaultValue="json") String format,Authentication auth){
   UserAccount user=management.requireUser(auth.getName());List<Map<String,Object>> rows=reports.studentWise(courseSectionId,from,to,auth.getName(),user.getRole());return render("Student-wise attendance report",courseSectionId,from,to,format,rows);
 }
 @GetMapping("/defaulters") public ResponseEntity<?> defaulters(@RequestParam Long courseSectionId,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(required=false) Integer threshold,@RequestParam(defaultValue="json") String format,Authentication auth){
   UserAccount user=management.requireUser(auth.getName());List<Map<String,Object>> rows=reports.defaulters(courseSectionId,from,to,auth.getName(),user.getRole(),threshold);return render("Defaulter report",courseSectionId,from,to,format,rows);
 }
 private ResponseEntity<?> render(String title,Long id,LocalDate from,LocalDate to,String format,List<Map<String,Object>> rows){
   String f=format.toLowerCase(Locale.ROOT);
   if("csv".equals(f)){byte[] content=reports.csv(rows);return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv; charset=UTF-8")).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"sams-report-"+id+".csv\"").body(content);}
   if("pdf".equals(f)){byte[] content=reports.pdf(title,id,from,to,rows);return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"sams-report-"+id+".pdf\"").body(content);}
   if(!"json".equals(f))throw edu.pesu.sams.exception.SamsException.badRequest("format must be json, csv or pdf");
   return ResponseEntity.ok(Map.of("title",title,"courseSectionId",id,"from",from.toString(),"to",to.toString(),"rows",rows,"count",rows.size()));
 }
}
