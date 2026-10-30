package edu.pesu.sams.controller;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1")
public class AttendanceController {
 private final AttendanceService attendance; private final UserManagementService management;
 public AttendanceController(AttendanceService attendance,UserManagementService management){this.attendance=attendance;this.management=management;}
 @PostMapping("/sessions") @PreAuthorize("hasRole('FACULTY')") public ResponseEntity<Map<String,Object>> createSession(@Valid @RequestBody CreateSessionRequest request,Authentication auth,HttpServletRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(attendance.createSession(auth.getName(),request,req));}
 @GetMapping("/sessions") public List<Map<String,Object>> sessions(Authentication auth){UserAccount u=management.requireUser(auth.getName());return attendance.listSessions(auth.getName(),u.getRole());}
 @GetMapping("/sessions/{id}/roster") @PreAuthorize("hasRole('FACULTY')") public Map<String,Object> roster(@PathVariable Long id,Authentication auth){return attendance.roster(id,auth.getName());}
 @PostMapping("/sessions/{id}/attendance") @PreAuthorize("hasRole('FACULTY')") public ResponseEntity<Map<String,Object>> submit(@PathVariable Long id,@Valid @RequestBody AttendanceSubmissionRequest request,Authentication auth,HttpServletRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(attendance.submitAttendance(id,request,auth.getName(),req));}
 @PutMapping("/sessions/{id}/attendance/{studentId}") @PreAuthorize("hasAnyRole('FACULTY','ADMIN')") public Map<String,Object> edit(@PathVariable Long id,@PathVariable String studentId,@Valid @RequestBody EditAttendanceRequest request,Authentication auth,HttpServletRequest req){UserAccount u=management.requireUser(auth.getName());return attendance.editAttendance(id,studentId,request,auth.getName(),u.getRole(),req);}
 @GetMapping("/students/{studentId}/attendance") @PreAuthorize("hasRole('STUDENT')") public Map<String,Object> ownAttendance(@PathVariable String studentId,Authentication auth){return attendance.ownSummary(studentId,auth.getName());}
 @GetMapping("/course-sections/{id}/attendance") @PreAuthorize("hasAnyRole('FACULTY','ADMIN')") public Map<String,Object> sectionAttendance(@PathVariable Long id,Authentication auth){UserAccount u=management.requireUser(auth.getName());return attendance.sectionAttendance(id,auth.getName(),u.getRole());}
}
