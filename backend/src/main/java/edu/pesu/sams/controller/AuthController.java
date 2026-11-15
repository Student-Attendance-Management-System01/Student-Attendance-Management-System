package edu.pesu.sams.controller;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.entity.UserAccount;
import edu.pesu.sams.exception.SamsException;
import edu.pesu.sams.repository.UserAccountRepository;
import edu.pesu.sams.service.AuthService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;
import java.time.LocalDateTime;
import java.util.*;

@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
 private final AuthService authService; private final UserAccountRepository users;
 @Value("${sams.cookie.secure:false}") private boolean secureCookie;
 @Value("${sams.jwt.lifetime-minutes:15}") private long lifetimeMinutes;
 public AuthController(AuthService authService,UserAccountRepository users){this.authService=authService;this.users=users;}
 @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken token){return Map.of("token",token.getToken());}
 @PostMapping("/login") public ResponseEntity<Map<String,Object>> login(@Valid @RequestBody LoginRequest request,HttpServletRequest servletRequest){
   UserAccount user=authService.authenticate(request.username(),request.password(),servletRequest.getRemoteAddr());String token=authService.issueToken(user);
   ResponseCookie cookie=ResponseCookie.from("SAMS_TOKEN",token).httpOnly(true).secure(secureCookie).sameSite("Strict").path("/").maxAge(lifetimeMinutes*60).build();
   Map<String,Object> body=new LinkedHashMap<>();body.put("userId",user.getInstitutionId());body.put("fullName",user.getFullName());body.put("email",user.getEmail());body.put("role",user.getRole().name());body.put("expiresInSeconds",lifetimeMinutes*60);
   return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie.toString()).body(body);
 }
 @PostMapping("/logout") public ResponseEntity<Void> logout(HttpServletRequest request){
   Cookie cookie=WebUtils.getCookie(request,"SAMS_TOKEN");authService.logout(cookie==null?null:cookie.getValue());
   ResponseCookie clear=ResponseCookie.from("SAMS_TOKEN","").httpOnly(true).secure(secureCookie).sameSite("Strict").path("/").maxAge(0).build();
   return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,clear.toString()).build();
 }
 @GetMapping("/me") public Map<String,Object> me(Authentication authentication){
   UserAccount user=users.findByEmailIgnoreCase(authentication.getName()).orElseThrow(()->SamsException.unauthorized("Authentication required"));
   return Map.of("userId",user.getInstitutionId(),"fullName",user.getFullName(),"email",user.getEmail(),"role",user.getRole().name());
 }
 @PostMapping("/password-reset/request") public ResponseEntity<Map<String,String>> requestReset(@Valid @RequestBody ResetRequest request){authService.requestPasswordReset(request.email());return ResponseEntity.accepted().body(Map.of("message","If the account exists, a password reset link has been sent."));}
 @PostMapping("/password-reset/confirm") public ResponseEntity<Void> confirmReset(@Valid @RequestBody ResetConfirmRequest request){authService.confirmPasswordReset(request.token(),request.newPassword());return ResponseEntity.noContent().build();}
}
