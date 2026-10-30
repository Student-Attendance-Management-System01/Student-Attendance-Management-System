package edu.pesu.sams.security;

import edu.pesu.sams.repository.RevokedTokenRepository;
import edu.pesu.sams.repository.UserAccountRepository;
import edu.pesu.sams.entity.UserAccount;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtCookieFilter extends OncePerRequestFilter {
 private final JwtService jwtService; private final RevokedTokenRepository revokedTokens; private final UserAccountRepository users;
 public JwtCookieFilter(JwtService jwtService,RevokedTokenRepository revokedTokens,UserAccountRepository users){this.jwtService=jwtService;this.revokedTokens=revokedTokens;this.users=users;}
 @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
   if(SecurityContextHolder.getContext().getAuthentication()==null){
     String token=null; Cookie[] cookies=request.getCookies();
     if(cookies!=null) for(Cookie c:cookies) if("SAMS_TOKEN".equals(c.getName())){token=c.getValue();break;}
     if(token!=null&&!token.isBlank()) try{
       Claims claims=jwtService.parse(token); String jti=claims.getId(); String email=claims.getSubject(); String role=claims.get("role",String.class);
       if(jti!=null&&email!=null&&role!=null&&!revokedTokens.existsByJti(jti)){
         // Re-check active status and role so deactivated or demoted accounts lose access immediately.
         UserAccount account=users.findByEmailIgnoreCase(email).orElse(null);
         if(account!=null&&account.isActive()&&account.getRole().name().equals(role)){
           var auth=new UsernamePasswordAuthenticationToken(email,null,List.of(new SimpleGrantedAuthority("ROLE_"+account.getRole().name())));
           auth.setDetails(request.getRemoteAddr());SecurityContextHolder.getContext().setAuthentication(auth);
         }
       }
     }catch(Exception ignored){ /* Invalid/expired cookies stay unauthenticated; never expose parser details. */ }
   }
   chain.doFilter(request,response);
 }
}
