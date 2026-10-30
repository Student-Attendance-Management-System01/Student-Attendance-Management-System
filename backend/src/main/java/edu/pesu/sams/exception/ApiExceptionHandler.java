package edu.pesu.sams.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(SamsException.class)
 ResponseEntity<Map<String,Object>> sams(SamsException ex,HttpServletRequest req){return response(ex.getStatus(),ex.getCode(),ex.getMessage(),req);}
 @ExceptionHandler(MethodArgumentNotValidException.class)
 ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException ex,HttpServletRequest req){
   Map<String,String> details=new LinkedHashMap<>(); ex.getBindingResult().getFieldErrors().forEach(e->details.putIfAbsent(e.getField(),e.getDefaultMessage()));
   Map<String,Object> body=base(HttpStatus.BAD_REQUEST,"SAMS-VAL-400","Validation failed",req);body.put("details",details);return ResponseEntity.badRequest().body(body);
 }
 @ExceptionHandler(ConstraintViolationException.class)
 ResponseEntity<Map<String,Object>> constraint(ConstraintViolationException ex,HttpServletRequest req){return response(HttpStatus.BAD_REQUEST,"SAMS-VAL-400","Validation failed",req);}
 @ExceptionHandler(DataIntegrityViolationException.class)
 ResponseEntity<Map<String,Object>> conflict(DataIntegrityViolationException ex,HttpServletRequest req){return response(HttpStatus.CONFLICT,"SAMS-DATA-409","The request conflicts with existing data",req);}
 @ExceptionHandler(AccessDeniedException.class)
 ResponseEntity<Map<String,Object>> denied(AccessDeniedException ex,HttpServletRequest req){return response(HttpStatus.FORBIDDEN,"SAMS-AUTH-403","You are not permitted to perform this action",req);}
 @ExceptionHandler(Exception.class)
 ResponseEntity<Map<String,Object>> unexpected(Exception ex,HttpServletRequest req){return response(HttpStatus.INTERNAL_SERVER_ERROR,"SAMS-SYS-500","An unexpected error occurred",req);}
 private ResponseEntity<Map<String,Object>> response(HttpStatus s,String code,String msg,HttpServletRequest req){return ResponseEntity.status(s).body(base(s,code,msg,req));}
 private Map<String,Object> base(HttpStatus s,String code,String msg,HttpServletRequest req){Map<String,Object> m=new LinkedHashMap<>();m.put("timestamp",OffsetDateTime.now(ZoneId.of("Asia/Kolkata")).toString());m.put("status",s.value());m.put("code",code);m.put("message",msg);m.put("path",req.getRequestURI());return m;}
}
