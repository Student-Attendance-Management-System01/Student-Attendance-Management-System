package edu.pesu.sams.controller;

import edu.pesu.sams.entity.UserAccount;
import edu.pesu.sams.entity.UserRole;
import edu.pesu.sams.repository.UserAccountRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Exercises sign-in, sign-out and role checks through the real security filter chain (in-memory H2). */
@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:sams-auth;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "management.health.mail.enabled=false",
    "sams.jwt.secret=c2Ftcy1pbnRlZ3JhdGlvbi10ZXN0LXNlY3JldC1rZXktMDEyMzQ1Njc4OQ=="
})
@AutoConfigureMockMvc
class AuthControllerTest {
 private static final String PASSWORD="Str0ng!Pass";
 @Autowired MockMvc mvc; @Autowired UserAccountRepository users; @Autowired PasswordEncoder encoder;

 @BeforeEach void seed(){
   user("ADM001","admin@example.edu",UserRole.ADMIN);user("FAC001","faculty@example.edu",UserRole.FACULTY);
   user("STU001","student@example.edu",UserRole.STUDENT);user("STU002","locked@example.edu",UserRole.STUDENT);
 }
 private void user(String id,String email,UserRole role){
   if(users.existsByEmailIgnoreCase(email))return;
   UserAccount u=new UserAccount();u.setInstitutionId(id);u.setFullName(id+" Name");u.setEmail(email);u.setRole(role);u.setPasswordHash(encoder.encode(PASSWORD));users.save(u);
 }
 private ResultActions login(String username,String password)throws Exception{
   return mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"username\":\""+username+"\",\"password\":\""+password+"\"}"));
 }
 private Cookie tokenFor(String email)throws Exception{
   String header=login(email,PASSWORD).andExpect(status().isOk()).andReturn().getResponse().getHeader(HttpHeaders.SET_COOKIE);
   assertNotNull(header);return new Cookie("SAMS_TOKEN",header.substring("SAMS_TOKEN=".length(),header.indexOf(';')));
 }

 @Test void validCredentialsIssueHttpOnlyTokenCookie()throws Exception{
   login("Student@Example.edu",PASSWORD).andExpect(status().isOk())
     .andExpect(jsonPath("$.userId").value("STU001")).andExpect(jsonPath("$.role").value("STUDENT"))
     .andExpect(header().string(HttpHeaders.SET_COOKIE,allOf(startsWith("SAMS_TOKEN="),containsString("HttpOnly"),containsString("SameSite=Strict"))));
 }
 @Test void wrongPasswordAndUnknownUserGetTheSameResponse()throws Exception{
   String wrongPassword=login("faculty@example.edu","Wr0ng!Pass").andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
   String unknownUser=login("nobody@example.edu","Wr0ng!Pass").andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
   assertEquals(wrongPassword.replaceAll("\"timestamp\":\"[^\"]*\"",""),unknownUser.replaceAll("\"timestamp\":\"[^\"]*\"",""));
   assertTrue(wrongPassword.contains("Invalid credentials"));assertFalse(wrongPassword.contains(PASSWORD));
 }
 @Test void repeatedFailuresLockTheAccount()throws Exception{
   for(int i=0;i<4;i++)login("locked@example.edu","Wr0ng!Pass").andExpect(status().isUnauthorized());
   login("locked@example.edu","Wr0ng!Pass").andExpect(status().isLocked());
   login("locked@example.edu",PASSWORD).andExpect(status().isLocked());
 }
 @Test void protectedEndpointsRequireAuthentication()throws Exception{
   mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("SAMS-AUTH-401"));
   mvc.perform(get("/api/v1/sessions")).andExpect(status().isUnauthorized());
   mvc.perform(get("/api/v1/auth/me").cookie(new Cookie("SAMS_TOKEN","not-a-jwt"))).andExpect(status().isUnauthorized());
   mvc.perform(get("/api/v1/auth/me").cookie(tokenFor("admin@example.edu"))).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"));
 }
 @Test void rolesCannotReachOtherRolesOperations()throws Exception{
   Cookie student=tokenFor("student@example.edu"),faculty=tokenFor("faculty@example.edu");
   mvc.perform(get("/api/v1/admin/users").cookie(student)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SAMS-AUTH-403"));
   mvc.perform(get("/api/v1/admin/users").cookie(faculty)).andExpect(status().isForbidden());
   mvc.perform(get("/api/v1/sessions/1/roster").cookie(student)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SAMS-AUTH-403"));
   mvc.perform(get("/api/v1/students/STU001/attendance").cookie(faculty)).andExpect(status().isForbidden());
 }
 @Test void logoutRevokesTheToken()throws Exception{
   Cookie token=tokenFor("faculty@example.edu");
   mvc.perform(get("/api/v1/auth/me").cookie(token)).andExpect(status().isOk());
   mvc.perform(post("/api/v1/auth/logout").with(csrf()).cookie(token)).andExpect(status().isNoContent()).andExpect(header().string(HttpHeaders.SET_COOKIE,containsString("Max-Age=0")));
   mvc.perform(get("/api/v1/auth/me").cookie(token)).andExpect(status().isUnauthorized());
 }
}
