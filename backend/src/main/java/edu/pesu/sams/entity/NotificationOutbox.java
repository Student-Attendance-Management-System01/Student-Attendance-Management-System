package edu.pesu.sams.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity @Table(name="notification_outbox", indexes={@Index(name="idx_outbox_status_next", columnList="status,next_attempt_at")})
public class NotificationOutbox {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="recipient_email", nullable=false, length=190) private String recipientEmail;
    @Column(nullable=false, length=200) private String subject;
    @Column(name="body_text", nullable=false, columnDefinition="TEXT") private String bodyText;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16) private OutboxStatus status=OutboxStatus.PENDING;
    @Column(nullable=false) private int attempts=0;
    @Column(name="next_attempt_at", nullable=false) private LocalDateTime nextAttemptAt;
    @Column(name="last_error", length=500) private String lastError;
    @Column(name="attachment_name", length=200) private String attachmentName;
    @Column(name="attachment_content_type", length=120) private String attachmentContentType;
    @Lob @Column(name="attachment_content", columnDefinition="LONGBLOB") private byte[] attachmentContent;
    @Column(name="created_at", nullable=false) private LocalDateTime createdAt;
    @Column(name="delivered_at") private LocalDateTime deliveredAt;
    @PrePersist void prePersist(){if(createdAt==null)createdAt=LocalDateTime.now(ZoneId.of("Asia/Kolkata"));if(nextAttemptAt==null)nextAttemptAt=createdAt;}
    public NotificationOutbox() {}
    public NotificationOutbox(String recipientEmail,String subject,String bodyText,LocalDateTime now){this.recipientEmail=recipientEmail;this.subject=subject;this.bodyText=bodyText;this.createdAt=now;this.nextAttemptAt=now;}
    public Long getId(){return id;} public String getRecipientEmail(){return recipientEmail;} public String getSubject(){return subject;} public String getBodyText(){return bodyText;} public OutboxStatus getStatus(){return status;} public void setStatus(OutboxStatus v){status=v;} public int getAttempts(){return attempts;} public void setAttempts(int v){attempts=v;} public LocalDateTime getNextAttemptAt(){return nextAttemptAt;} public void setNextAttemptAt(LocalDateTime v){nextAttemptAt=v;} public String getLastError(){return lastError;} public void setLastError(String v){lastError=v;} public String getAttachmentName(){return attachmentName;} public String getAttachmentContentType(){return attachmentContentType;} public byte[] getAttachmentContent(){return attachmentContent;} public void setAttachment(String name,String contentType,byte[] content){this.attachmentName=name;this.attachmentContentType=contentType;this.attachmentContent=content;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getDeliveredAt(){return deliveredAt;} public void setDeliveredAt(LocalDateTime v){deliveredAt=v;}
}
