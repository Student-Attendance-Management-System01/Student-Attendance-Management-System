package edu.pesu.sams.config;

import edu.pesu.sams.security.JwtCookieFilter;
import edu.pesu.sams.security.RateLimitFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.IOException;

@Configuration
public class SecurityConfiguration {
 @Value("${sams.cookie.secure:false}") private boolean secureCookie;
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
 @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtCookieFilter jwtCookieFilter,RateLimitFilter rateLimitFilter)throws Exception{
   CookieCsrfTokenRepository csrfRepo=CookieCsrfTokenRepository.withHttpOnlyFalse();
   csrfRepo.setCookiePath("/"); csrfRepo.setCookieCustomizer(c->c.secure(secureCookie).sameSite("Strict"));
   CsrfTokenRequestAttributeHandler csrfHandler=new CsrfTokenRequestAttributeHandler();
   http.csrf(csrf->csrf.csrfTokenRepository(csrfRepo).csrfTokenRequestHandler(csrfHandler))
     .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
     .formLogin(form->form.disable()).httpBasic(basic->basic.disable())
     .logout(logout->logout.disable())
     .authorizeHttpRequests(auth->auth
       .requestMatchers("/actuator/health/**","/api/v1/auth/csrf","/api/v1/auth/login","/api/v1/auth/password-reset/request","/api/v1/auth/password-reset/confirm").permitAll()
       .requestMatchers("/api/v1/admin/**","/api/v1/audit-log").hasRole("ADMIN")
       .anyRequest().authenticated())
     .exceptionHandling(ex->ex
       .authenticationEntryPoint((req,res,e)->writeError(res,401,"SAMS-AUTH-401","Authentication required"))
       .accessDeniedHandler((req,res,e)->writeError(res,403,"SAMS-AUTH-403","You are not permitted to perform this action")))
     .addFilterBefore(rateLimitFilter,UsernamePasswordAuthenticationFilter.class)
     .addFilterBefore(jwtCookieFilter,UsernamePasswordAuthenticationFilter.class)
     .headers(headers->headers.frameOptions(frame->frame.sameOrigin()));
   return http.build();
 }
 private static void writeError(HttpServletResponse response,int status,String code,String message)throws IOException{
   response.setStatus(status);response.setContentType(MediaType.APPLICATION_JSON_VALUE);response.getWriter().write("{\"status\":"+status+",\"code\":\""+code+"\",\"message\":\""+message+"\"}");
 }
}
