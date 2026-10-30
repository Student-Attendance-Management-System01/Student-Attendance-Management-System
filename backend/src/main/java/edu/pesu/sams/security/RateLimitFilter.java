package edu.pesu.sams.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.*;
import java.util.concurrent.*;

/** In-memory sliding-window guard. In a multi-replica production deployment use a shared store. */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
 private final ConcurrentMap<String, ConcurrentLinkedDeque<Instant>> visits=new ConcurrentHashMap<>();
 private final Clock clock;
 public RateLimitFilter(Clock clock){this.clock=clock;}
 @Override protected boolean shouldNotFilter(HttpServletRequest request){return !request.getRequestURI().startsWith("/api/")||"/api/v1/auth/csrf".equals(request.getRequestURI())||"OPTIONS".equalsIgnoreCase(request.getMethod());}
 @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
   String ip=request.getRemoteAddr()==null?"unknown":request.getRemoteAddr();boolean login="/api/v1/auth/login".equals(request.getRequestURI());int limit=login?20:100;String key=ip+":"+(login?"login":"general");Instant now=clock.instant(),cutoff=now.minusSeconds(60);
   ConcurrentLinkedDeque<Instant> queue=visits.computeIfAbsent(key,k->new ConcurrentLinkedDeque<>());
   synchronized(queue){while(!queue.isEmpty()&&queue.peekFirst().isBefore(cutoff))queue.pollFirst();if(queue.size()>=limit){response.setStatus(429);response.setContentType(MediaType.APPLICATION_JSON_VALUE);response.setHeader("Retry-After","60");response.getWriter().write("{\"status\":429,\"code\":\"SAMS-RATE-429\",\"message\":\"Too many requests. Please try again shortly.\"}");return;}queue.addLast(now);}
   if(visits.size()>10000){for(var entry:visits.entrySet()){ConcurrentLinkedDeque<Instant> q=entry.getValue();synchronized(q){while(!q.isEmpty()&&q.peekFirst().isBefore(cutoff))q.pollFirst();if(q.isEmpty())visits.remove(entry.getKey(),q);}}}
   chain.doFilter(request,response);
 }
}
