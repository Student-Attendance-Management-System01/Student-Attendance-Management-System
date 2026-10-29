package edu.pesu.sams.controller;

import edu.pesu.sams.entity.UserAccount;
import edu.pesu.sams.entity.UserRole;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Student management APIs through the real security filter chain (in-memory H2). */
@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:sams-students;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "management.health.mail.enabled=false",
    "sams.jwt.secret=c2Ftcy1pbnRlZ3JhdGlvbi10ZXN0LXNlY3JldC1rZXktMDEyMzQ1Njc4OQ=="
})
@AutoConfigureMockMvc
class StudentControllerTest {
 private static final String HASH="$2a$12$placeholderhashplaceholderhashplaceholderhashplacehold";
 @Autowired MockMvc mvc; @Autowired UserAccountRepository users; @Autowired JwtService jwt;
 private Cookie admin,faculty,student;

 @BeforeEach void seed(){
   users.deleteAll();
   admin=token(user("ADM001","admin@example.edu",UserRole.ADMIN,true));faculty=token(user("FAC001","faculty@example.edu",UserRole.FACULTY,true));
   student=token(user("PES1UG24CS001","asha@example.edu",UserRole.STUDENT,true));
   user("PES1UG24CS002","ravi@example.edu",UserRole.STUDENT,true);user("PES1UG24CS003","meera@example.edu",UserRole.STUDENT,false);
 }
 private UserAccount user(String id,String email,UserRole role,boolean active){
   UserAccount u=new UserAccount();u.setInstitutionId(id);u.setFullName(email.substring(0,email.indexOf('@'))+" Kumar");u.setEmail(email);u.setRole(role);u.setPasswordHash(HASH);u.setActive(active);return users.save(u);
 }
 private Cookie token(UserAccount u){return new Cookie("SAMS_TOKEN",jwt.create(u));}
 private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder b,String body){return b.with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body);}
 private static final String NEW_STUDENT="{\"institutionId\":\"pes1ug24cs010\",\"fullName\":\"Kiran Rao\",\"email\":\"Kiran@Example.edu\",\"password\":\"Str0ng!Pass\"}";

 @Test void adminCreatesAndViewsStudent()throws Exception{
   mvc.perform(json(post("/api/v1/admin/students"),NEW_STUDENT).cookie(admin)).andExpect(status().isCreated())
     .andExpect(jsonPath("$.institutionId").value("PES1UG24CS010")).andExpect(jsonPath("$.email").value("kiran@example.edu"))
     .andExpect(jsonPath("$.active").value(true)).andExpect(jsonPath("$.passwordHash").doesNotExist()).andExpect(jsonPath("$.role").doesNotExist());
   mvc.perform(get("/api/v1/admin/students/pes1ug24cs010").cookie(admin)).andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Kiran Rao"));
   mvc.perform(get("/api/v1/admin/students/NOPE").cookie(admin)).andExpect(status().isNotFound());
   mvc.perform(get("/api/v1/admin/students/FAC001").cookie(admin)).andExpect(status().isNotFound());
 }
 @Test void invalidOrMissingFieldsAreReported()throws Exception{
   mvc.perform(json(post("/api/v1/admin/students"),"{\"institutionId\":\"PES-1 UG\",\"fullName\":\"\",\"email\":\"not-an-email\",\"password\":\"Str0ng!Pass\"}").cookie(admin))
     .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SAMS-VAL-400"))
     .andExpect(jsonPath("$.details.institutionId").exists()).andExpect(jsonPath("$.details.fullName").exists()).andExpect(jsonPath("$.details.email").exists());
   mvc.perform(json(post("/api/v1/admin/students"),"{\"institutionId\":\"PES1UG24CS011\",\"fullName\":\"Weak Pass\",\"email\":\"weak@example.edu\",\"password\":\"password1\"}").cookie(admin))
     .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(containsString("Password")));
 }
 @Test void duplicateIdentifiersAreRejectedNamingTheField()throws Exception{
   mvc.perform(json(post("/api/v1/admin/students"),"{\"institutionId\":\"pes1ug24cs001\",\"fullName\":\"Copy\",\"email\":\"copy@example.edu\",\"password\":\"Str0ng!Pass\"}").cookie(admin))
     .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("institutionId already exists"));
   mvc.perform(json(post("/api/v1/admin/students"),"{\"institutionId\":\"PES1UG24CS099\",\"fullName\":\"Copy\",\"email\":\"ASHA@example.edu\",\"password\":\"Str0ng!Pass\"}").cookie(admin))
     .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("email already exists"));
   mvc.perform(json(put("/api/v1/admin/students/PES1UG24CS002"),"{\"fullName\":\"Ravi\",\"email\":\"asha@example.edu\"}").cookie(admin))
     .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("email already exists"));
 }
 @Test void listSupportsSearchFilterAndPagination()throws Exception{
   mvc.perform(get("/api/v1/admin/students").cookie(admin)).andExpect(status().isOk())
     .andExpect(jsonPath("$.totalElements").value(3)).andExpect(jsonPath("$.content[*].institutionId",contains("PES1UG24CS001","PES1UG24CS002","PES1UG24CS003")));
   mvc.perform(get("/api/v1/admin/students").param("search","RAVI").cookie(admin)).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].email").value("ravi@example.edu"));
   mvc.perform(get("/api/v1/admin/students").param("search","%").cookie(admin)).andExpect(jsonPath("$.totalElements").value(0));
   mvc.perform(get("/api/v1/admin/students").param("active","false").cookie(admin)).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].institutionId").value("PES1UG24CS003"));
   mvc.perform(get("/api/v1/admin/students").param("page","1").param("size","2").cookie(admin))
     .andExpect(jsonPath("$.content",hasSize(1))).andExpect(jsonPath("$.page").value(1)).andExpect(jsonPath("$.totalPages").value(2));
   mvc.perform(get("/api/v1/admin/students").param("size","500").cookie(admin)).andExpect(status().isBadRequest());
 }
 @Test void adminUpdatesAndDeactivatesStudent()throws Exception{
   mvc.perform(json(put("/api/v1/admin/students/PES1UG24CS002"),"{\"fullName\":\"Ravi Shankar\",\"email\":\"ravi.s@example.edu\",\"active\":false}").cookie(admin))
     .andExpect(status().isOk()).andExpect(jsonPath("$.institutionId").value("PES1UG24CS002"))
     .andExpect(jsonPath("$.fullName").value("Ravi Shankar")).andExpect(jsonPath("$.email").value("ravi.s@example.edu")).andExpect(jsonPath("$.active").value(false));
   mvc.perform(json(put("/api/v1/admin/students/FAC001"),"{\"fullName\":\"Not A Student\",\"email\":\"faculty@example.edu\"}").cookie(admin)).andExpect(status().isNotFound());
 }
 @Test void nonAdminsCannotManageStudents()throws Exception{
   mvc.perform(get("/api/v1/admin/students")).andExpect(status().isUnauthorized());
   mvc.perform(json(post("/api/v1/admin/students"),NEW_STUDENT)).andExpect(status().isUnauthorized());
   for(Cookie c:new Cookie[]{student,faculty}){
     mvc.perform(get("/api/v1/admin/students").cookie(c)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SAMS-AUTH-403"));
     mvc.perform(get("/api/v1/admin/students/PES1UG24CS002").cookie(c)).andExpect(status().isForbidden());
     mvc.perform(json(post("/api/v1/admin/students"),NEW_STUDENT).cookie(c)).andExpect(status().isForbidden());
     mvc.perform(json(put("/api/v1/admin/students/PES1UG24CS002"),"{\"fullName\":\"Hacked\",\"email\":\"ravi@example.edu\"}").cookie(c)).andExpect(status().isForbidden());
   }
   mvc.perform(get("/api/v1/admin/students/PES1UG24CS002").cookie(admin)).andExpect(jsonPath("$.fullName").value("ravi Kumar"));
 }
 @Test void studentSeesOnlyOwnProfile()throws Exception{
   mvc.perform(get("/api/v1/students/me").cookie(student)).andExpect(status().isOk())
     .andExpect(jsonPath("$.institutionId").value("PES1UG24CS001")).andExpect(jsonPath("$.passwordHash").doesNotExist());
   mvc.perform(get("/api/v1/students/me").cookie(faculty)).andExpect(status().isForbidden());
   mvc.perform(get("/api/v1/students/me")).andExpect(status().isUnauthorized());
 }
}
