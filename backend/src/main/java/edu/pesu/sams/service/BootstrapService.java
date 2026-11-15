package edu.pesu.sams.service;

import edu.pesu.sams.entity.*;
import edu.pesu.sams.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapService implements CommandLineRunner {
 private final AppSettingRepository settings; private final UserAccountRepository users; private final PasswordEncoder encoder; private final PasswordPolicy passwordPolicy;
 @Value("${sams.attendance.default-threshold:75}") private int defaultThreshold;
 @Value("${sams.bootstrap.admin-email:}") private String adminEmail;
 @Value("${sams.bootstrap.admin-password:}") private String adminPassword;
 @Value("${sams.bootstrap.admin-institution-id:}") private String adminId;
 @Value("${sams.bootstrap.admin-name:}") private String adminName;
 public BootstrapService(AppSettingRepository settings,UserAccountRepository users,PasswordEncoder encoder,PasswordPolicy passwordPolicy){this.settings=settings;this.users=users;this.encoder=encoder;this.passwordPolicy=passwordPolicy;}
 @Override @Transactional public void run(String... args){
   if(settings.findByKey("attendance.threshold").isEmpty())settings.save(new AppSetting("attendance.threshold",String.valueOf(defaultThreshold)));
   if(adminEmail!=null&&!adminEmail.isBlank()&&!users.existsByEmailIgnoreCase(adminEmail)){
     if(adminPassword==null||adminPassword.isBlank()||adminId==null||adminId.isBlank()||adminName==null||adminName.isBlank())throw new IllegalStateException("Bootstrap admin is configured incompletely. Set SAMS_BOOTSTRAP_ADMIN_EMAIL, _PASSWORD, _ID and _NAME together.");
     passwordPolicy.validate(adminPassword);
     UserAccount admin=new UserAccount();admin.setInstitutionId(adminId.trim());admin.setFullName(adminName.trim());admin.setEmail(adminEmail.trim().toLowerCase());admin.setRole(UserRole.ADMIN);admin.setPasswordHash(encoder.encode(adminPassword));admin.setActive(true);users.save(admin);
   }
 }
}
