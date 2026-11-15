package edu.pesu.sams.service;

import edu.pesu.sams.entity.*;
import edu.pesu.sams.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sams-scheduler-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "management.health.mail.enabled=false",
        "sams.jwt.secret=c2Ftcy1pbnRlZ3JhdGlvbi10ZXN0LXNlY3JldC1rZXktMDEyMzQ1Njc4OQ=="
})
class ReportServiceTest {
    @Autowired ReportService reportService;
    @Autowired ReportScheduler reportScheduler;
    @Autowired UserAccountRepository users;
    @Autowired CourseSectionRepository sections;
    @Autowired EnrollmentRepository enrolments;
    @Autowired ClassSessionRepository sessions;
    @Autowired AttendanceRecordRepository records;
    @Autowired NotificationOutboxRepository outbox;
    @Autowired ReportLogRepository reportLogs;

    private CourseSection section;
    private UserAccount adminUser, facultyUser, studentUser;

    @BeforeEach
    void setup() {
        outbox.deleteAll();
        reportLogs.deleteAll();
        records.deleteAll();
        sessions.deleteAll();
        enrolments.deleteAll();
        sections.deleteAll();
        users.deleteAll();

        adminUser = new UserAccount();
        adminUser.setInstitutionId("ADM001");
        adminUser.setFullName("Admin One");
        adminUser.setEmail("admin@example.edu");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setPasswordHash("$2a$12$hash");
        adminUser.setActive(true);
        users.save(adminUser);

        facultyUser = new UserAccount();
        facultyUser.setInstitutionId("EMP001");
        facultyUser.setFullName("Faculty One");
        facultyUser.setEmail("faculty@example.edu");
        facultyUser.setRole(UserRole.FACULTY);
        facultyUser.setPasswordHash("$2a$12$hash");
        facultyUser.setActive(true);
        users.save(facultyUser);

        studentUser = new UserAccount();
        studentUser.setInstitutionId("PES1UG24CS001");
        studentUser.setFullName("=HYPERLINK(\"http://evil.com\")"); // formula injection test name
        studentUser.setEmail("student@example.edu");
        studentUser.setRole(UserRole.STUDENT);
        studentUser.setPasswordHash("$2a$12$hash");
        studentUser.setActive(true);
        users.save(studentUser);

        section = new CourseSection();
        section.setCourseCode("CS201");
        section.setCourseName("Data Structures");
        section.setSemester("3");
        section.setSection("A");
        section.setAcademicYear("2026-27");
        section.setActive(true);
        section.setFaculty(new HashSet<>(List.of(facultyUser)));
        sections.save(section);

        enrolments.save(new Enrollment(section, studentUser));

        ClassSession s = new ClassSession();
        s.setCourseSection(section);
        s.setDate(LocalDate.now().minusDays(3));
        s.setStartTime(LocalTime.of(9, 0));
        s.setEndTime(LocalTime.of(10, 0));
        s.setType(SessionType.LECTURE);
        s.setCreatedBy(facultyUser);
        s.setSubmitted(true);
        sessions.save(s);

        records.save(new AttendanceRecord(s, studentUser, AttendanceStatus.PRESENT));
    }

    @Test
    void csvFormulaInjectionEscapesSpecialCharacters() {
        List<Map<String, Object>> rows = List.of(
                Map.of(
                        "studentId", "@PES1UG",
                        "name", "=HYPERLINK(\"http://evil.com\")",
                        "email", "+admin@evil.com",
                        "held", 1,
                        "attended", 1,
                        "percentage", "100.00"
                )
        );

        byte[] csvBytes = reportService.csv(rows);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // Verify that formulas starting with =, @, + have single quote prepended
        assertTrue(csv.contains("'@PES1UG"), "Should escape @ prefix");
        assertTrue(csv.contains("'=HYPERLINK"), "Should escape = prefix");
        assertTrue(csv.contains("'+admin@evil.com"), "Should escape + prefix");
    }

    @Test
    void weeklyReportSchedulerEnqueuesEmailAndLog() {
        reportScheduler.weeklyReports();

        // Faculty should have received an attachment email
        List<NotificationOutbox> notifications = outbox.findAll();
        assertFalse(notifications.isEmpty());
        assertTrue(notifications.stream().anyMatch(n -> n.getRecipientEmail().equals("faculty@example.edu") && n.getAttachmentContent() != null));

        // Report log should record WEEKLY report
        List<ReportLog> logs = reportLogs.findAll();
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(l -> "WEEKLY".equals(l.getReportType())));
    }

    @Test
    void monthlyReportSchedulerEnqueuesEmailAndLog() {
        reportScheduler.monthlyConsolidatedReport();

        // Admin should have received monthly consolidated report email
        List<NotificationOutbox> notifications = outbox.findAll();
        assertFalse(notifications.isEmpty());
        assertTrue(notifications.stream().anyMatch(n -> n.getRecipientEmail().equals("admin@example.edu") && n.getAttachmentContent() != null));

        // Report log should record MONTHLY report
        List<ReportLog> logs = reportLogs.findAll();
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(l -> "MONTHLY".equals(l.getReportType())));
    }
}
