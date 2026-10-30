package edu.pesu.sams.service;

import edu.pesu.sams.entity.AuditLog;
import edu.pesu.sams.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
 private final AuditLogRepository repository;
 public AuditService(AuditLogRepository repository){this.repository=repository;}
 @Transactional public void record(String actorEmail,String action,String targetType,String targetId,String oldValue,String newValue,String reason,HttpServletRequest request){
   String ip=request==null?null:request.getRemoteAddr();
   repository.save(new AuditLog(actorEmail,action,targetType,targetId,oldValue,newValue,reason,ip));
 }
 @Transactional public void record(String actorEmail,String action,String targetType,String targetId,String oldValue,String newValue,String reason){
   repository.save(new AuditLog(actorEmail,action,targetType,targetId,oldValue,newValue,reason,null));
 }
}
