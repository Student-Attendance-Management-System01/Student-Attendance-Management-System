package edu.pesu.sams.service;

import com.opencsv.CSVWriter;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@Component
public class ReportScheduler {
 private final CourseSectionRepository sections; private final UserAccountRepository users; private final ReportService reports; private final NotificationService notifications; private final ReportLogRepository reportLogs; private final Clock clock;
 public ReportScheduler(CourseSectionRepository sections,UserAccountRepository users,ReportService reports,NotificationService notifications,ReportLogRepository reportLogs,Clock clock){this.sections=sections;this.users=users;this.reports=reports;this.notifications=notifications;this.reportLogs=reportLogs;this.clock=clock;}
 @Scheduled(cron="0 0 6 * * MON", zone="Asia/Kolkata") @Transactional
 public void weeklyReports(){LocalDate today=LocalDate.now(clock),from=today.minusDays(7),to=today.minusDays(1);
   for(CourseSection section:sections.findByActiveTrueOrderByCourseCodeAsc()){
     List<Map<String,Object>> rows=reports.sectionAttendanceForScheduled(section.getId(),from,to);byte[] attachment=reports.csv(rows);
     for(UserAccount faculty:section.getFaculty()){
       notifications.enqueueAttachment(faculty.getEmail(),"SAMS weekly attendance report — "+section.getCourseCode(),"Attached is the weekly attendance report for "+section.getCourseCode()+" / "+section.getSection()+" from "+from+" to "+to+".","weekly-attendance-"+section.getCourseCode()+".csv","text/csv",attachment);
       reportLogs.save(new ReportLog("WEEKLY",faculty.getEmail(),"QUEUED","Section "+section.getId()+", range "+from+" to "+to,LocalDateTime.now(clock)));
     }
   }
 }
 @Scheduled(cron="0 0 6 1 * *", zone="Asia/Kolkata") @Transactional
 public void monthlyConsolidatedReport(){LocalDate today=LocalDate.now(clock),from=today.minusMonths(1).withDayOfMonth(1),to=today.withDayOfMonth(1).minusDays(1);List<Map<String,Object>> all=new ArrayList<>();
   for(CourseSection section:sections.findByActiveTrueOrderByCourseCodeAsc())for(Map<String,Object> r:reports.sectionAttendanceForScheduled(section.getId(),from,to)){Map<String,Object> row=new LinkedHashMap<>();row.put("courseCode",section.getCourseCode());row.put("section",section.getSection());row.putAll(r);all.add(row);}
   byte[] bytes=monthlyCsv(all);List<UserAccount> admins=users.findByRoleAndActiveTrue(UserRole.ADMIN);
   for(UserAccount admin:admins){notifications.enqueueAttachment(admin.getEmail(),"SAMS monthly consolidated attendance report","Attached is the consolidated attendance report from "+from+" to "+to+".","monthly-attendance.csv","text/csv",bytes);reportLogs.save(new ReportLog("MONTHLY",admin.getEmail(),"QUEUED","Sections="+sections.findByActiveTrueOrderByCourseCodeAsc().size()+", range "+from+" to "+to,LocalDateTime.now(clock)));}
 }
 private byte[] monthlyCsv(List<Map<String,Object>> rows){StringWriter out=new StringWriter();try(CSVWriter csv=new CSVWriter(out)){csv.writeNext(new String[]{"Course Code","Section","Student ID","Name","Email","Sessions Held","Sessions Attended","Attendance Percentage"});for(Map<String,Object> r:rows)csv.writeNext(new String[]{safe(r.get("courseCode")),safe(r.get("section")),safe(r.get("studentId")),safe(r.get("name")),safe(r.get("email")),safe(r.get("held")),safe(r.get("attended")),safe(r.get("percentage"))});}catch(Exception e){throw new IllegalStateException("Could not create monthly CSV report");}return out.toString().getBytes(StandardCharsets.UTF_8);}
 private String safe(Object v){String s=v==null?"":String.valueOf(v);return !s.isEmpty()&&"=+-@".indexOf(s.charAt(0))>=0?"'"+s:s;}
}
