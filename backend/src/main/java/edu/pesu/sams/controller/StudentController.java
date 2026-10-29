package edu.pesu.sams.controller;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Student administration is admin-only (/admin/** in SecurityConfiguration); students may only read their own profile. */
@RestController @RequestMapping("/api/v1")
public class StudentController {
 private final StudentService students;
 public StudentController(StudentService students){this.students=students;}
 @GetMapping("/admin/students") public PageResponse<StudentResponse> list(@RequestParam(required=false) String search,@RequestParam(required=false) Boolean active,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return students.list(search,active,page,size);}
 @GetMapping("/admin/students/{institutionId}") public StudentResponse get(@PathVariable String institutionId){return students.get(institutionId);}
 @PostMapping("/admin/students") public ResponseEntity<StudentResponse> create(@Valid @RequestBody CreateStudentRequest request,Authentication auth,HttpServletRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(students.create(request,auth.getName(),req));}
 @PutMapping("/admin/students/{institutionId}") public StudentResponse update(@PathVariable String institutionId,@Valid @RequestBody UpdateStudentRequest request,Authentication auth,HttpServletRequest req){return students.update(institutionId,request,auth.getName(),req);}
 @GetMapping("/students/me") @PreAuthorize("hasRole('STUDENT')") public StudentResponse me(Authentication auth){return students.own(auth.getName());}
}
