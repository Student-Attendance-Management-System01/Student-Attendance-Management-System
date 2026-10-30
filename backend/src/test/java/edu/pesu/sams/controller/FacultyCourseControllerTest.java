package edu.pesu.sams.controller;

import edu.pesu.sams.entity.CourseSection;
import edu.pesu.sams.entity.UserAccount;
import edu.pesu.sams.entity.UserRole;
import edu.pesu.sams.repository.CourseSectionRepository;
import edu.pesu.sams.repository.UserAccountRepository;
import edu.pesu.sams.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Faculty and course-section management APIs through the real security filter chain (in-memory H2). */
@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:sams-faculty-course;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "management.health.mail.enabled=false",
    "sams.jwt.secret=c2Ftcy1pbnRlZ3JhdGlvbi10ZXN0LXNlY3JldC1rZXktMDEyMzQ1Njc4OQ=="
})
@AutoConfigureMockMvc
class FacultyCourseControllerTest {
 private static final String HASH="$2a$12$placeholderhashplaceholderhashplaceholderhashplacehold";
 @Autowired MockMvc mvc; @Autowired UserAccountRepository users; @Autowired CourseSectionRepository sections; @Autowired JwtService jwt;
 private Cookie admin,facultyCookie,studentCookie; private UserAccount fac1,fac2,inactiveFac,student; private CourseSection se;

 @BeforeEach void seed(){
   sections.deleteAll();users.deleteAll();
   admin=token(user("ADM001","admin@example.edu",UserRole.ADMIN,true));
   fac1=user("EMP001","priya@example.edu",UserRole.FACULTY,true);fac2=user("EMP002","arun@example.edu",UserRole.FACULTY,true);
   inactiveFac=user("EMP003","old@example.edu",UserRole.FACULTY,false);student=user("PES1UG24CS001","asha@example.edu",UserRole.STUDENT,true);
   facultyCookie=token(fac1);studentCookie=token(student);
   CourseSection s=new CourseSection();s.setCourseCode("UE23CS341A");s.setCourseName("Software Engineering");s.setSemester("5");s.setSection("A");s.setAcademicYear("2026-27");s.setFaculty(new HashSet<>(List.of(fac1)));se=sections.save(s);
 }
 private UserAccount user(String id,String email,UserRole role,boolean active){
   UserAccount u=new UserAccount();u.setInstitutionId(id);u.setFullName(email.substring(0,email.indexOf('@'))+" Rao");u.setEmail(email);u.setRole(role);u.setPasswordHash(HASH);u.setActive(active);return users.save(u);
 }
 private Cookie token(UserAccount u){return new Cookie("SAMS_TOKEN",jwt.create(u));}
 private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder b,String body){return b.with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body);}
 private String section(String code,String semester,String sec,String year,Long... facultyIds){
   return "{\"courseCode\":\""+code+"\",\"courseName\":\"Data Structures\",\"semester\":\""+semester+"\",\"section\":\""+sec+"\",\"academicYear\":\""+year+"\",\"facultyIds\":"+Arrays.toString(facultyIds)+"}";
 }
 private static final String NEW_FACULTY="{\"institutionId\":\"emp010\",\"fullName\":\"Lakshmi Iyer\",\"email\":\"Lakshmi@Example.edu\",\"password\":\"Str0ng!Pass\"}";

 @Test void adminCreatesAndViewsFaculty()throws Exception{
   mvc.perform(json(post("/api/v1/admin/faculty"),NEW_FACULTY).cookie(admin)).andExpect(status().isCreated())
     .andExpect(jsonPath("$.institutionId").value("EMP010")).andExpect(jsonPath("$.email").value("lakshmi@example.edu"))
     .andExpect(jsonPath("$.sections",hasSize(0))).andExpect(jsonPath("$.passwordHash").doesNotExist());
   mvc.perform(get("/api/v1/admin/faculty/emp001").cookie(admin)).andExpect(status().isOk())
     .andExpect(jsonPath("$.fullName").value("priya Rao")).andExpect(jsonPath("$.sections[0].courseCode").value("UE23CS341A"));
   mvc.perform(get("/api/v1/admin/faculty/PES1UG24CS001").cookie(admin)).andExpect(status().isNotFound());
   mvc.perform(get("/api/v1/admin/faculty").cookie(admin)).andExpect(status().isOk()).andExpect(jsonPath("$[*].institutionId",containsInAnyOrder("EMP001","EMP002","EMP010")));
 }
 @Test void facultyValidationAndDuplicates()throws Exception{
   mvc.perform(json(post("/api/v1/admin/faculty"),"{\"institutionId\":\"EMP 01!\",\"fullName\":\"\",\"email\":\"bad\",\"password\":\"Str0ng!Pass\"}").cookie(admin))
     .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.institutionId").exists()).andExpect(jsonPath("$.details.fullName").exists()).andExpect(jsonPath("$.details.email").exists());
   mvc.perform(json(post("/api/v1/admin/faculty"),"{\"institutionId\":\"emp001\",\"fullName\":\"Copy\",\"email\":\"copy@example.edu\",\"password\":\"Str0ng!Pass\"}").cookie(admin))
     .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("institutionId already exists"));
   mvc.perform(json(post("/api/v1/admin/faculty"),"{\"institutionId\":\"EMP099\",\"fullName\":\"Copy\",\"email\":\"PRIYA@example.edu\",\"password\":\"Str0ng!Pass\"}").cookie(admin))
     .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("email already exists"));
 }
 @Test void lastActiveFacultyOfASectionCannotBeDeactivated()throws Exception{
   mvc.perform(json(put("/api/v1/admin/faculty/EMP001"),"{\"fullName\":\"Priya Rao\",\"email\":\"priya@example.edu\",\"active\":false}").cookie(admin))
     .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SAMS-FAC-409"));
   mvc.perform(json(put("/api/v1/admin/course-sections/"+se.getId()),section("UE23CS341A","5","A","2026-27",fac1.getId(),fac2.getId()).replace("}",",\"active\":true}")).cookie(admin)).andExpect(status().isOk());
   mvc.perform(json(put("/api/v1/admin/faculty/EMP001"),"{\"fullName\":\"Priya Rao\",\"email\":\"priya.r@example.edu\",\"active\":false}").cookie(admin))
     .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false)).andExpect(jsonPath("$.email").value("priya.r@example.edu"));
 }
 @Test void adminCreatesAndViewsCourseSection()throws Exception{
   String body=mvc.perform(json(post("/api/v1/admin/course-sections"),section("cs201","3","b","2026-27",fac1.getId(),fac2.getId(),fac1.getId())).cookie(admin))
     .andExpect(status().isCreated()).andExpect(jsonPath("$.courseCode").value("CS201")).andExpect(jsonPath("$.section").value("B"))
     .andExpect(jsonPath("$.faculty[*].institutionId",contains("EMP001","EMP002"))).andReturn().getResponse().getContentAsString();
   String id=body.replaceAll("^\\{\"id\":(\\d+),.*","$1");
   mvc.perform(get("/api/v1/admin/course-sections/"+id).cookie(admin)).andExpect(status().isOk()).andExpect(jsonPath("$.courseName").value("Data Structures"));
   mvc.perform(get("/api/v1/admin/course-sections/999999").cookie(admin)).andExpect(status().isNotFound());
 }
 @Test void invalidCourseSectionsAreRejected()throws Exception{
   mvc.perform(json(post("/api/v1/admin/course-sections"),section("UE23CS341A","5","a","2026-27",fac2.getId())).cookie(admin))
     .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SAMS-CRS-409"));
   mvc.perform(json(post("/api/v1/admin/course-sections"),section("TOOLONGCODE1","9","ABC","26-27",fac1.getId())).cookie(admin))
     .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.courseCode").exists()).andExpect(jsonPath("$.details.semester").exists())
     .andExpect(jsonPath("$.details.section").exists()).andExpect(jsonPath("$.details.academicYear").exists());
   mvc.perform(json(post("/api/v1/admin/course-sections"),section("CS202","3","A","2026-28",fac1.getId())).cookie(admin))
     .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(containsString("consecutive")));
   mvc.perform(json(post("/api/v1/admin/course-sections"),"{\"courseCode\":\"CS202\",\"courseName\":\"DS\",\"semester\":\"3\",\"section\":\"A\",\"academicYear\":\"2026-27\",\"facultyIds\":[]}").cookie(admin))
     .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.facultyIds").exists());
   for(Long bad:new Long[]{student.getId(),inactiveFac.getId(),999999L})
     mvc.perform(json(post("/api/v1/admin/course-sections"),section("CS202","3","A","2026-27",bad)).cookie(admin)).andExpect(status().isBadRequest());
 }
 @Test void adminUpdatesCourseSection()throws Exception{
   mvc.perform(json(post("/api/v1/admin/course-sections"),section("CS201","3","B","2026-27",fac1.getId())).cookie(admin)).andExpect(status().isCreated());
   mvc.perform(json(put("/api/v1/admin/course-sections/"+se.getId()),section("UE23CS341A","5","A","2026-27",fac2.getId()).replace("}",",\"active\":false}")).cookie(admin))
     .andExpect(status().isOk()).andExpect(jsonPath("$.courseName").value("Data Structures")).andExpect(jsonPath("$.active").value(false))
     .andExpect(jsonPath("$.faculty[*].institutionId",contains("EMP002")));
   mvc.perform(json(put("/api/v1/admin/course-sections/"+se.getId()),section("CS201","3","B","2026-27",fac2.getId())).cookie(admin))
     .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SAMS-CRS-409"));
 }
 @Test void onlyAdminsCanManageFacultyAndCourses()throws Exception{
   mvc.perform(get("/api/v1/admin/faculty/EMP001")).andExpect(status().isUnauthorized());
   mvc.perform(json(post("/api/v1/admin/course-sections"),section("CS203","3","A","2026-27",fac1.getId()))).andExpect(status().isUnauthorized());
   for(Cookie c:new Cookie[]{facultyCookie,studentCookie}){
     mvc.perform(json(post("/api/v1/admin/faculty"),NEW_FACULTY).cookie(c)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SAMS-AUTH-403"));
     mvc.perform(get("/api/v1/admin/faculty/EMP002").cookie(c)).andExpect(status().isForbidden());
     mvc.perform(json(put("/api/v1/admin/faculty/EMP002"),"{\"fullName\":\"Hacked\",\"email\":\"arun@example.edu\"}").cookie(c)).andExpect(status().isForbidden());
     mvc.perform(json(post("/api/v1/admin/course-sections"),section("CS203","3","A","2026-27",fac1.getId())).cookie(c)).andExpect(status().isForbidden());
     mvc.perform(get("/api/v1/admin/course-sections/"+se.getId()).cookie(c)).andExpect(status().isForbidden());
     mvc.perform(json(put("/api/v1/admin/course-sections/"+se.getId()),section("UE23CS341A","5","A","2026-27",fac1.getId())).cookie(c)).andExpect(status().isForbidden());
   }
   mvc.perform(get("/api/v1/admin/faculty/EMP002").cookie(admin)).andExpect(jsonPath("$.fullName").value("arun Rao"));
 }
}
