package edu.pesu.sams.service;

import edu.pesu.sams.entity.*;
import edu.pesu.sams.exception.SamsException;
import edu.pesu.sams.repository.*;
import edu.pesu.sams.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
public class AuthService {
 private final UserAccountRepository users; private final PasswordEncoder passwordEncoder; private final PasswordPolicy passwordPolicy;
 private final JwtService jwtService; private final PasswordResetTokenRepository resetTokens; private final RevokedTokenRepository revokedTokens;
 private final NotificationService notifications; private final AuditService audit; private final Clock clock;
 @Value("${sams.frontend-url:http://localhost:5173}") private String frontendUrl;
 public AuthService(UserAccountRepository users,PasswordEncoder passwordEncoder,PasswordPolicy passwordPolicy,JwtService jwtService,PasswordResetTokenRepository resetTokens,RevokedTokenRepository revokedTokens,NotificationService notifications,AuditService audit,Clock clock){
   this.users=users;this.passwordEncoder=passwordEncoder;this.passwordPolicy=passwordPolicy;this.jwtService=jwtService;this.resetTokens=resetTokens;this.revokedTokens=revokedTokens;this.notifications=notifications;this.audit=audit;this.clock=clock;
 }
 @Transactional(noRollbackFor = SamsException.class) public UserAccount authenticate(String username,String password,String ip){
   String email=username.trim().toLowerCase(Locale.ROOT); LocalDateTime now=LocalDateTime.now(clock);
   UserAccount user=users.findByEmailIgnoreCase(email).orElse(null);
   // Hash anyway so unknown accounts take as long to reject as wrong passwords.
   if(user==null){passwordEncoder.matches(password,dummyHash());audit.record(null,"LOGIN_FAILED","USER","unknown-user",null,null,"Invalid credentials from "+safeIp(ip));throw SamsException.unauthorized("Invalid credentials");}
   if(user.getLockedUntil()!=null&&user.getLockedUntil().isAfter(now))throw new SamsException(HttpStatus.LOCKED,"SAMS-AUTH-423","Account temporarily locked");
   if(user.getLockedUntil()!=null&&!user.getLockedUntil().isAfter(now)){user.setLockedUntil(null);user.setFailedLoginAttempts(0);}
   if(!user.isActive()||!passwordEncoder.matches(password,user.getPasswordHash())){
     int attempts=user.getFailedLoginAttempts()+1;user.setFailedLoginAttempts(attempts);
     if(attempts>=5){user.setLockedUntil(now.plusMinutes(15));user.setFailedLoginAttempts(0);}
     audit.record(null,"LOGIN_FAILED","USER",user.getInstitutionId(),null,null,"Invalid credentials from "+safeIp(ip));
     if(user.getLockedUntil()!=null&&user.getLockedUntil().isAfter(now))throw new SamsException(HttpStatus.LOCKED,"SAMS-AUTH-423","Account temporarily locked");
     throw SamsException.unauthorized("Invalid credentials");
   }
   user.setFailedLoginAttempts(0);user.setLockedUntil(null);audit.record(user.getEmail(),"LOGIN_SUCCESS","USER",user.getInstitutionId(),null,null,"Login succeeded");return user;
 }
 public String issueToken(UserAccount user){return jwtService.create(user);}
 @Transactional public void logout(String token){
   if(token==null||token.isBlank())return;
   try{Claims claims=jwtService.parse(token);String jti=claims.getId();if(jti!=null&&!revokedTokens.existsByJti(jti))revokedTokens.save(new RevokedToken(jti,jwtService.localExpiry(claims)));}catch(Exception ignored){/* Cookie can be expired or malformed; always clear it. */}
 }
 @Transactional public void requestPasswordReset(String email){
   users.findByEmailIgnoreCase(email.trim()).filter(UserAccount::isActive).ifPresent(user->{
     byte[] random=new byte[32];new SecureRandom().nextBytes(random);String token=Base64.getUrlEncoder().withoutPadding().encodeToString(random);
     resetTokens.save(new PasswordResetToken(user,sha256(token),LocalDateTime.now(clock).plusMinutes(30)));
     String link=frontendUrl.replaceAll("/$","")+"/reset-password?token="+token;
     notifications.enqueue(user.getEmail(),"SAMS password reset","A password reset was requested for your SAMS account. Open this link within 30 minutes to choose a new password:\n\n"+link+"\n\nIf you did not request this, ignore this message.");
   });
 }
 @Transactional public void confirmPasswordReset(String rawToken,String newPassword){
   passwordPolicy.validate(newPassword);String hash=sha256(rawToken);
   PasswordResetToken reset=resetTokens.findByTokenHash(hash).orElseThrow(()->new SamsException(HttpStatus.GONE,"SAMS-AUTH-410","Reset token is invalid, expired or already used"));
   LocalDateTime now=LocalDateTime.now(clock);
   if(reset.getUsedAt()!=null||reset.getExpiresAt().isBefore(now))throw new SamsException(HttpStatus.GONE,"SAMS-AUTH-410","Reset token is invalid, expired or already used");
   reset.getUser().setPasswordHash(passwordEncoder.encode(newPassword));reset.getUser().setFailedLoginAttempts(0);reset.getUser().setLockedUntil(null);reset.setUsedAt(now);
   audit.record(reset.getUser().getEmail(),"PASSWORD_RESET","USER",reset.getUser().getInstitutionId(),null,"PASSWORD_CHANGED","Password reset succeeded");
 }
 private String sha256(String v){try{byte[] digest=MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8));return HexFormat.of().formatHex(digest);}catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}}
 private volatile String dummyHash;
 private String dummyHash(){if(dummyHash==null)dummyHash=passwordEncoder.encode(UUID.randomUUID().toString());return dummyHash;}
 private String safeIp(String ip){return ip==null?"unknown":ip.replaceAll("[^0-9a-fA-F:.]","");}
}
