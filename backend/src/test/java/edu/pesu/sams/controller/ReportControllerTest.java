package edu.pesu.sams.controller;

import edu.pesu.sams.entity.*;
import edu.pesu.sams.repository.*;
import edu.pesu.sams.security.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sams-reports;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "management.health.mail.enabled=false",
        "sams.jwt.secret=c2Ftcy1pbnRlZ3JhdGlvbi10ZXN0LXNlY3JldC1rZXktMDEyMzQ1Njc4OQ=="
})
@AutoConfigureMockMvc
class ReportControllerTest {
    private static final String HASH = "$2a$12$placeholderhashplaceholderhashplaceholderhashplacehold";

    @Autowired MockMvc mvc;
    @Autowired UserAccountRepository users;
    @Autowired CourseSectionRepository sections;
    @Autowired EnrollmentRepository enrolments;
    @Autowired ClassSessionRepository sessions;
    @Autowired AttendanceRecordRepository records;
    @Autowired JwtService jwt;

    private Cookie admin, assignedFaculty, otherFaculty, studentCookie;
    private CourseSection secA;
    private UserAccount student1, student2;

    @BeforeEach
    void seed() {
        records.deleteAll();
        sessions.deleteAll();
        enrolments.deleteAll();
        sections.deleteAll();
        users.deleteAll();

        admin = token(user("ADM001", "admin@example.edu", UserRole.ADMIN, true));
        UserAccount fac1 = user("EMP001", "priya@example.edu", UserRole.FACULTY, true);
        UserAccount fac2 = user("EMP002", "arun@example.edu", UserRole.FACULTY, true);
        assignedFaculty = token(fac1);
        otherFaculty = token(fac2);

        student1 = user("PES1UG24CS001", "asha@example.edu", UserRole.STUDENT, true);
        student2 = user("PES1UG24CS002", "ravi@example.edu", UserRole.STUDENT, true);
        studentCookie = token(student1);

        secA = new CourseSection();
        secA.setCourseCode("CS201");
        secA.setCourseName("Data Structures");
        secA.setSemester("3");
        secA.setSection("A");
        secA.setAcademicYear("2026-27");
        secA.setActive(true);
        secA.setFaculty(new HashSet<>(List.of(fac1)));
        sections.save(secA);

        enrolments.save(new Enrollment(secA, student1));
        enrolments.save(new Enrollment(secA, student2));

        // Create 4 sessions: 3 attended by student1 (75%), 1 attended by student2 (25%)
        for (int i = 1; i <= 4; i++) {
            ClassSession s = new ClassSession();
            s.setCourseSection(secA);
            s.setDate(LocalDate.of(2026, 10, i));
            s.setStartTime(LocalTime.of(9, 0));
            s.setEndTime(LocalTime.of(10, 0));
            s.setType(SessionType.LECTURE);
            s.setCreatedBy(fac1);
            s.setSubmitted(true);
            sessions.save(s);

            // student1 present for sessions 1, 2, 3; absent for 4
            records.save(new AttendanceRecord(s, student1, i <= 3 ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT));

            // student2 present for session 1; absent for 2, 3, 4
            records.save(new AttendanceRecord(s, student2, i == 1 ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT));
        }
    }

    private UserAccount user(String id, String email, UserRole role, boolean active) {
        UserAccount u = new UserAccount();
        u.setInstitutionId(id);
        u.setFullName(email.substring(0, email.indexOf('@')) + " Rao");
        u.setEmail(email);
        u.setRole(role);
        u.setPasswordHash(HASH);
        u.setActive(active);
        return users.save(u);
    }

    private Cookie token(UserAccount u) {
        return new Cookie("SAMS_TOKEN", jwt.create(u));
    }

    @Test
    void studentWiseJsonReportCalculatesPercentages() throws Exception {
        mvc.perform(get("/api/v1/reports/student-wise")
                        .param("courseSectionId", secA.getId().toString())
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-10")
                        .param("format", "json")
                        .cookie(assignedFaculty))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.rows[0].studentId").value("PES1UG24CS001"))
                .andExpect(jsonPath("$.rows[0].held").value(4))
                .andExpect(jsonPath("$.rows[0].attended").value(3))
                .andExpect(jsonPath("$.rows[0].percentage").value(75.0))
                .andExpect(jsonPath("$.rows[1].studentId").value("PES1UG24CS002"))
                .andExpect(jsonPath("$.rows[1].held").value(4))
                .andExpect(jsonPath("$.rows[1].attended").value(1))
                .andExpect(jsonPath("$.rows[1].percentage").value(25.0));
    }

    @Test
    void defaultersReportFiltersByThreshold() throws Exception {
        // At default 75% threshold, student1 (75%) is NOT a defaulter (< 75), student2 (25%) IS a defaulter
        mvc.perform(get("/api/v1/reports/defaulters")
                        .param("courseSectionId", secA.getId().toString())
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-10")
                        .cookie(assignedFaculty))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.rows[0].studentId").value("PES1UG24CS002"));

        // At custom 80% threshold, BOTH student1 (75%) and student2 (25%) are defaulters (< 80)
        mvc.perform(get("/api/v1/reports/defaulters")
                        .param("courseSectionId", secA.getId().toString())
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-10")
                        .param("threshold", "80")
                        .cookie(assignedFaculty))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    void csvReportGeneratesValidContent() throws Exception {
        MvcResult res = mvc.perform(get("/api/v1/reports/student-wise")
                        .param("courseSectionId", secA.getId().toString())
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-10")
                        .param("format", "csv")
                        .cookie(assignedFaculty))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"sams-report-")))
                .andReturn();

        String body = res.getResponse().getContentAsString();
        assertTrue(body.contains("Student ID"));
        assertTrue(body.contains("PES1UG24CS001"));
        assertTrue(body.contains("75.00"));
    }

    @Test
    void pdfReportGeneratesValidPdfBytes() throws Exception {
        MvcResult res = mvc.perform(get("/api/v1/reports/student-wise")
                        .param("courseSectionId", secA.getId().toString())
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-10")
                        .param("format", "pdf")
                        .cookie(assignedFaculty))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"sams-report-")))
                .andReturn();

        byte[] pdfBytes = res.getResponse().getContentAsByteArray();
        assertTrue(pdfBytes.length > 0);
        // Check standard PDF magic header %PDF-
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
        assertEquals('-', (char) pdfBytes[4]);
    }

    @Test
    void rolePermissionsEnforced() throws Exception {
        // Admin can view any section's reports
        mvc.perform(get("/api/v1/reports/student-wise")
                        .param("courseSectionId", secA.getId().toString())
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-10")
                        .cookie(admin))
                .andExpect(status().isOk());

        // Unassigned faculty cannot view reports
        mvc.perform(get("/api/v1/reports/student-wise")
                        .param("courseSectionId", secA.getId().toString())
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-10")
                        .cookie(otherFaculty))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SAMS-AUTH-403"));

        // Student cannot view reports
        mvc.perform(get("/api/v1/reports/student-wise")
                        .param("courseSectionId", secA.getId().toString())
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-10")
                        .cookie(studentCookie))
                .andExpect(status().isForbidden());

        // Anonymous cannot view reports
        mvc.perform(get("/api/v1/reports/student-wise")
                        .param("courseSectionId", secA.getId().toString())
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidDateRangeOrThresholdRejected() throws Exception {
        // from > to
        mvc.perform(get("/api/v1/reports/student-wise")
                        .param("courseSectionId", secA.getId().toString())
                        .param("from", "2026-10-10")
                        .param("to", "2026-10-01")
                        .cookie(assignedFaculty))
                .andExpect(status().isBadRequest());

        // threshold out of 1-100 range
        mvc.perform(get("/api/v1/reports/defaulters")
                        .param("courseSectionId", secA.getId().toString())
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-10")
                        .param("threshold", "150")
                        .cookie(assignedFaculty))
                .andExpect(status().isBadRequest());
    }
}
