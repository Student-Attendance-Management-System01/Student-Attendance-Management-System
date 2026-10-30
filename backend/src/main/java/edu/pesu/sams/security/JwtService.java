package edu.pesu.sams.security;

import edu.pesu.sams.entity.UserAccount;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.*;
import java.util.*;

@Service
public class JwtService {
 @Value("${sams.jwt.secret:}") private String secret;
 @Value("${sams.jwt.issuer:sams}") private String issuer;
 @Value("${sams.jwt.lifetime-minutes:15}") private long lifetimeMinutes;
 private SecretKey key(){
   if(secret==null||secret.isBlank()) throw new IllegalStateException("SAMS_JWT_SECRET must be set. Use a randomly generated Base64 secret of at least 32 bytes.");
   try{return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));}catch(Exception ex){throw new IllegalStateException("SAMS_JWT_SECRET must be Base64-encoded and contain at least 32 bytes.");}
 }
 public String create(UserAccount user){
   Instant now=Instant.now();
   return Jwts.builder().issuer(issuer).subject(user.getEmail()).claim("role",user.getRole().name()).claim("institutionId",user.getInstitutionId())
    .id(UUID.randomUUID().toString()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(lifetimeMinutes*60)))
    .signWith(key()).compact();
 }
 public Claims parse(String token){return Jwts.parser().verifyWith(key()).requireIssuer(issuer).build().parseSignedClaims(token).getPayload();}
 public long getLifetimeSeconds(){return lifetimeMinutes*60;}
 public LocalDateTime localExpiry(Claims claims){return LocalDateTime.ofInstant(claims.getExpiration().toInstant(),ZoneId.of("Asia/Kolkata"));}
}
