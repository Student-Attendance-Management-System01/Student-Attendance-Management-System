package edu.pesu.sams.controller;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Faculty and course-section administration; everything here is under /admin/** and therefore admin-only (SecurityConfiguration). */
@RestController @RequestMapping("/api/v1/admin")
public class FacultyCourseController {
 private final FacultyService faculty; private final CourseSectionService sections;
 public FacultyCourseController(FacultyService faculty,CourseSectionService sections){this.faculty=faculty;this.sections=sections;}
 @PostMapping("/faculty") public ResponseEntity<FacultyResponse> createFaculty(@Valid @RequestBody CreateFacultyRequest request,Authentication auth,HttpServletRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(faculty.create(request,auth.getName(),req));}
 @GetMapping("/faculty/{institutionId}") public FacultyResponse getFaculty(@PathVariable String institutionId){return faculty.get(institutionId);}
 @PutMapping("/faculty/{institutionId}") public FacultyResponse updateFaculty(@PathVariable String institutionId,@Valid @RequestBody UpdateFacultyRequest request,Authentication auth,HttpServletRequest req){return faculty.update(institutionId,request,auth.getName(),req);}
 @PostMapping("/course-sections") public ResponseEntity<CourseSectionResponse> createSection(@Valid @RequestBody CourseSectionRequest request,Authentication auth,HttpServletRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(sections.create(request,auth.getName(),req));}
 @GetMapping("/course-sections/{id}") public CourseSectionResponse getSection(@PathVariable Long id){return sections.get(id);}
 @PutMapping("/course-sections/{id}") public CourseSectionResponse updateSection(@PathVariable Long id,@Valid @RequestBody UpdateCourseSectionRequest request,Authentication auth,HttpServletRequest req){return sections.update(id,request,auth.getName(),req);}
}
