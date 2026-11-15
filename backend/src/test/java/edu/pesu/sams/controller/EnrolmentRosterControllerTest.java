package edu.pesu.sams.controller;

import edu.pesu.sams.entity.CourseSection;
import edu.pesu.sams.entity.Enrollment;
import edu.pesu.sams.entity.UserAccount;
import edu.pesu.sams.entity.UserRole;
import edu.pesu.sams.repository.CourseSectionRepository;
import edu.pesu.sams.repository.EnrollmentRepository;
import edu.pesu.sams.repository.UserAccountRepository;
import edu.pesu.sams.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Enrolment and class roster APIs through the real security filter chain (in-memory H2). */
@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:sams-enrolment;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "management.health.mail.enabled=false",
    "sams.jwt.secret=c2Ftcy1pbnRlZ3JhdGlvbi10ZXN0LXNlY3JldC1rZXktMDEyMzQ1Njc4OQ=="
})
@AutoConfigureMockMvc
class EnrolmentRosterControllerTest {
 private static final String HASH="$2a$12$placeholderhashplaceholderhashplaceholderhashplacehold";
 @Autowired MockMvc mvc; @Autowired UserAccountRepository users; @Autowired CourseSectionRepository sections; @Autowired EnrollmentRepository enrolments; @Autowired JwtService jwt;
 private Cookie admin,assignedFaculty,otherFaculty,studentCookie; private CourseSection secA,secB,closed;

 @BeforeEach void seed(){
   enrolments.deleteAll();sections.deleteAll();users.deleteAll();
   admin=token(user("ADM001","admin@example.edu",UserRole.ADMIN,true));
   UserAccount fac1=user("EMP001","priya@example.edu",UserRole.FACULTY,true),fac2=user("EMP002","arun@example.edu",UserRole.FACULTY,true);
   assignedFaculty=token(fac1);otherFaculty=token(fac2);
   studentCookie=token(user("PES1UG24CS001","asha@example.edu",UserRole.STUDENT,true));
   user("PES1UG24CS002","ravi@example.edu",UserRole.STUDENT,true);user("PES1UG24CS003","meera@example.edu",UserRole.STUDENT,false);
   secA=section("CS201","A",true,fac1);secB=section("CS202","B",true,fac2);closed=section("CS203","C",false,fac1);
 }
 private UserAccount user(String id,String email,UserRole role,boolean active){
   UserAccount u=new UserAccount();u.setInstitutionId(id);u.setFullName(email.substring(0,email.indexOf('@'))+" Rao");u.setEmail(email);u.setRole(role);u.setPasswordHash(HASH);u.setActive(active);return users.save(u);
 }
 private CourseSection section(String code,String sec,boolean active,UserAccount faculty){
   CourseSection s=new CourseSection();s.setCourseCode(code);s.setCourseName("Data Structures");s.setSemester("3");s.setSection(sec);s.setAcademicYear("2026-27");s.setActive(active);s.setFaculty(new HashSet<>(List.of(faculty)));return sections.save(s);
 }
 private Cookie token(UserAccount u){return new Cookie("SAMS_TOKEN",jwt.create(u));}
 private MockHttpServletRequestBuilder enrol(CourseSection s,String institutionId){
   return post("/api/v1/admin/course-sections/"+s.getId()+"/enrolments").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"institutionId\":\""+institutionId+"\"}");
 }
 private MockHttpServletRequestBuilder importCsv(CourseSection s,String csv){
   return multipart("/api/v1/admin/course-sections/"+s.getId()+"/enrolments/import").file(new MockMultipartFile("file","students.csv","text/csv",csv.getBytes(StandardCharsets.UTF_8))).with(csrf());
 }

 @Test void adminEnrolsStudentAndRosterShowsThem()throws Exception{
   mvc.perform(enrol(secA,"pes1ug24cs002").cookie(admin)).andExpect(status().isCreated()).andExpect(jsonPath("$.studentId").value("PES1UG24CS002"));
   mvc.perform(enrol(secA,"PES1UG24CS001").cookie(admin)).andExpect(status().isCreated());
   mvc.perform(get("/api/v1/course-sections/"+secA.getId()+"/roster").cookie(admin)).andExpect(status().isOk())
     .andExpect(jsonPath("$.courseCode").value("CS201")).andExpect(jsonPath("$.totalStudents").value(2))
     .andExpect(jsonPath("$.students[*].institutionId",contains("PES1UG24CS001","PES1UG24CS002"))).andExpect(jsonPath("$.students[0].passwordHash").doesNotExist());
   mvc.perform(get("/api/v1/admin/course-sections/"+secA.getId()+"/students").cookie(admin)).andExpect(status().isOk()).andExpect(jsonPath("$",hasSize(2)));
 }
 @Test void duplicateAndInvalidEnrolmentsAreRejected()throws Exception{
   mvc.perform(enrol(secA,"PES1UG24CS001").cookie(admin)).andExpect(status().isCreated());
   mvc.perform(enrol(secA,"pes1ug24cs001").cookie(admin)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SAMS-ENR-409"));
   mvc.perform(enrol(secA,"NOSUCHSTUDENT").cookie(admin)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(containsString("Student not found")));
   mvc.perform(enrol(secA,"EMP002").cookie(admin)).andExpect(status().isBadRequest());
   mvc.perform(enrol(secA,"PES1UG24CS003").cookie(admin)).andExpect(status().isBadRequest());
   mvc.perform(enrol(secA,"").cookie(admin)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.institutionId").exists());
   mvc.perform(enrol(closed,"PES1UG24CS002").cookie(admin)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(containsString("inactive")));
   mvc.perform(post("/api/v1/admin/course-sections/999999/enrolments").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"institutionId\":\"PES1UG24CS002\"}").cookie(admin)).andExpect(status().isNotFound());
   mvc.perform(get("/api/v1/admin/course-sections/999999/students").cookie(admin)).andExpect(status().isNotFound());
 }
 @Test void csvImportKeepsValidRowsAndReportsInvalidOnes()throws Exception{
   String csv="institutionId\nPES1UG24CS001\nPES1UG24CS002\npes1ug24cs002\nNOSUCHSTUDENT\nEMP001\n\"\"\nPES1UG24CS003\n";
   mvc.perform(importCsv(secA,csv).cookie(admin)).andExpect(status().isOk())
     .andExpect(jsonPath("$.imported").value(2)).andExpect(jsonPath("$.rejected").value(5))
     .andExpect(jsonPath("$.errors[*].row",contains(4,5,6,7,8)))
     .andExpect(jsonPath("$.errors[*].reason",contains("already enrolled","student not found","account is not an active student","institutionId is blank","account is not an active student")));
   mvc.perform(get("/api/v1/course-sections/"+secA.getId()+"/roster").cookie(admin)).andExpect(jsonPath("$.totalStudents").value(2));
   mvc.perform(importCsv(secA,"name\nAsha\n").cookie(admin)).andExpect(status().isBadRequest());
   mvc.perform(importCsv(closed,csv).cookie(admin)).andExpect(status().isBadRequest());
 }
 @Test void facultySeeOnlyRostersOfTheirOwnSections()throws Exception{
   enrolments.save(new Enrollment(secA,users.findByInstitutionIdIgnoreCase("PES1UG24CS001").orElseThrow()));
   mvc.perform(get("/api/v1/course-sections/"+secA.getId()+"/roster").cookie(assignedFaculty)).andExpect(status().isOk()).andExpect(jsonPath("$.students[0].institutionId").value("PES1UG24CS001"));
   mvc.perform(get("/api/v1/course-sections/"+secA.getId()+"/roster").cookie(otherFaculty)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SAMS-AUTH-403"));
   mvc.perform(get("/api/v1/course-sections/"+secB.getId()+"/roster").cookie(assignedFaculty)).andExpect(status().isForbidden());
   mvc.perform(get("/api/v1/course-sections/"+secA.getId()+"/roster").cookie(studentCookie)).andExpect(status().isForbidden());
   mvc.perform(get("/api/v1/course-sections/"+secA.getId()+"/roster")).andExpect(status().isUnauthorized());
   mvc.perform(get("/api/v1/course-sections/999999/roster").cookie(admin)).andExpect(status().isNotFound());
 }
 @Test void onlyAdminsCanEnrolStudents()throws Exception{
   for(Cookie c:new Cookie[]{assignedFaculty,studentCookie}){
     mvc.perform(enrol(secA,"PES1UG24CS002").cookie(c)).andExpect(status().isForbidden());
     mvc.perform(importCsv(secA,"institutionId\nPES1UG24CS002\n").cookie(c)).andExpect(status().isForbidden());
     mvc.perform(get("/api/v1/admin/course-sections/"+secA.getId()+"/students").cookie(c)).andExpect(status().isForbidden());
   }
   mvc.perform(enrol(secA,"PES1UG24CS002")).andExpect(status().isUnauthorized());
   mvc.perform(get("/api/v1/course-sections/"+secA.getId()+"/roster").cookie(admin)).andExpect(jsonPath("$.totalStudents").value(0));
 }
}
