package edu.pesu.sams.service;

import edu.pesu.sams.entity.*;
import edu.pesu.sams.repository.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ByteArrayResource;
import java.time.*;

@Service
public class NotificationService {
 private final NotificationOutboxRepository outbox; private final ObjectProvider<JavaMailSender> mailSenderProvider; private final Clock clock;
 @Value("${sams.email.enabled:false}") private boolean emailEnabled;
 @Value("${sams.email.from:no-reply@sams.local}") private String from;
 public NotificationService(NotificationOutboxRepository outbox,ObjectProvider<JavaMailSender> mailSenderProvider,Clock clock){this.outbox=outbox;this.mailSenderProvider=mailSenderProvider;this.clock=clock;}
 @Transactional public void enqueue(String to,String subject,String body){if(to==null||to.isBlank())return;outbox.save(new NotificationOutbox(to,subject,body,LocalDateTime.now(clock)));}
 @Transactional public void enqueueAttachment(String to,String subject,String body,String filename,String contentType,byte[] content){if(to==null||to.isBlank())return;NotificationOutbox n=new NotificationOutbox(to,subject,body,LocalDateTime.now(clock));n.setAttachment(filename,contentType,content);outbox.save(n);}
 @Scheduled(fixedDelay=300000, initialDelay=30000)
 @Transactional
 public void deliverPending(){
   LocalDateTime now=LocalDateTime.now(clock);
   for(NotificationOutbox item:outbox.findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(OutboxStatus.PENDING,now)){
     try{
       if(!emailEnabled)throw new IllegalStateException("E-mail delivery is disabled by configuration");
       JavaMailSender sender=mailSenderProvider.getIfAvailable(); if(sender==null)throw new IllegalStateException("SMTP sender is not configured");
       MimeMessage mime=sender.createMimeMessage();MimeMessageHelper helper=new MimeMessageHelper(mime,item.getAttachmentContent()!=null,"UTF-8");
       helper.setFrom(from);helper.setTo(item.getRecipientEmail());helper.setSubject(item.getSubject());helper.setText(item.getBodyText(),false);
       if(item.getAttachmentContent()!=null&&item.getAttachmentName()!=null){String type=item.getAttachmentContentType()==null?"application/octet-stream":item.getAttachmentContentType();helper.addAttachment(item.getAttachmentName(),new ByteArrayResource(item.getAttachmentContent()),type);}
       sender.send(mime);item.setStatus(OutboxStatus.DELIVERED);item.setDeliveredAt(now);item.setLastError(null);
     }catch(Exception ex){int attempts=item.getAttempts()+1;item.setAttempts(attempts);item.setLastError(safeError(ex));if(attempts>=4)item.setStatus(OutboxStatus.FAILED);else item.setNextAttemptAt(now.plusMinutes(5));}
     outbox.save(item);
   }
 }
 private String safeError(Exception ex){String s=ex.getClass().getSimpleName();return s.length()>500?s.substring(0,500):s;}
}
