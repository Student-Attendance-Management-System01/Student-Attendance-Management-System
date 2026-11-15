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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sams-corrections;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "management.health.mail.enabled=false",
        "sams.jwt.secret=c2Ftcy1pbnRlZ3JhdGlvbi10ZXN0LXNlY3JldC1rZXktMDEyMzQ1Njc4OQ=="
})
@AutoConfigureMockMvc
class CorrectionControllerTest {
    private static final String HASH = "$2a$12$placeholderhashplaceholderhashplaceholderhashplacehold";

    @Autowired MockMvc mvc;
    @Autowired UserAccountRepository users;
    @Autowired CourseSectionRepository sections;
    @Autowired ClassSessionRepository sessions;
    @Autowired AttendanceRecordRepository attendance;
    @Autowired CorrectionRequestRepository corrections;
    @Autowired AuditLogRepository audit;
    @Autowired JwtService jwt;

    private Cookie admin, assignedFaculty, otherFaculty, studentCookie, otherStudentCookie;
    private UserAccount studentUser, otherStudentUser, fac1, fac2;
    private CourseSection section;
    private AttendanceRecord recordRecent, recordOld, otherStudentRecord;

    @BeforeEach
    void seed() {
        corrections.deleteAll();
        attendance.deleteAll();
        sessions.deleteAll();
        sections.deleteAll();
        audit.deleteAll();
        users.deleteAll();

        admin = token(user("ADM001", "admin@example.edu", UserRole.ADMIN, true));
        fac1 = user("EMP001", "priya@example.edu", UserRole.FACULTY, true);
        fac2 = user("EMP002", "arun@example.edu", UserRole.FACULTY, true);
        assignedFaculty = token(fac1);
        otherFaculty = token(fac2);

        studentUser = user("PES1UG24CS001", "asha@example.edu", UserRole.STUDENT, true);
        otherStudentUser = user("PES1UG24CS002", "ravi@example.edu", UserRole.STUDENT, true);
        studentCookie = token(studentUser);
        otherStudentCookie = token(otherStudentUser);

        section = new CourseSection();
        section.setCourseCode("CS201");
        section.setCourseName("Data Structures");
        section.setSemester("3");
        section.setSection("A");
        section.setAcademicYear("2026-27");
        section.setActive(true);
        section.setFaculty(new HashSet<>(List.of(fac1)));
        sections.save(section);

        ClassSession sessionRecent = session(section, LocalDate.now().minusDays(2));
        ClassSession sessionOld = session(section, LocalDate.now().minusDays(10));

        recordRecent = record(sessionRecent, studentUser, AttendanceStatus.ABSENT);
        recordOld = record(sessionOld, studentUser, AttendanceStatus.ABSENT);
        otherStudentRecord = record(sessionRecent, otherStudentUser, AttendanceStatus.ABSENT);
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

    private ClassSession session(CourseSection sec, LocalDate date) {
        ClassSession s = new ClassSession();
        s.setCourseSection(sec);
        s.setDate(date);
        s.setStartTime(LocalTime.of(9, 0));
        s.setEndTime(LocalTime.of(10, 0));
        s.setType(SessionType.LECTURE);
        s.setCreatedBy(fac1);
        s.setSubmitted(true);
        return sessions.save(s);
    }

    private AttendanceRecord record(ClassSession s, UserAccount student, AttendanceStatus status) {
        return attendance.save(new AttendanceRecord(s, student, status));
    }

    private Cookie token(UserAccount u) {
        return new Cookie("SAMS_TOKEN", jwt.create(u));
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder b, String body) {
        return b.with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void studentCreatesCorrectionRequestWithin7DaysAndAuditLogged() throws Exception {
        String body = "{\"attendanceRecordId\":" + recordRecent.getId() + ",\"requestedStatus\":\"PRESENT\",\"reason\":\"Was present in class, misidentified during roll call\"}";
        mvc.perform(json(post("/api/v1/corrections"), body).cookie(studentCookie))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value("PES1UG24CS001"))
                .andExpect(jsonPath("$.courseCode").value("CS201"))
                .andExpect(jsonPath("$.currentStatus").value("ABSENT"))
                .andExpect(jsonPath("$.requestedStatus").value("PRESENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Verify audit log
        assertTrue(audit.findAll().stream().anyMatch(a -> "CORRECTION_REQUESTED".equals(a.getAction())));
    }

    @Test
    void studentCannotRequestCorrectionForAnotherStudent() throws Exception {
        String body = "{\"attendanceRecordId\":" + otherStudentRecord.getId() + ",\"requestedStatus\":\"PRESENT\",\"reason\":\"Present\"}";
        mvc.perform(json(post("/api/v1/corrections"), body).cookie(studentCookie))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SAMS-AUTH-403"));
    }

    @Test
    void correctionOlderThan7DaysIsRejected() throws Exception {
        String body = "{\"attendanceRecordId\":" + recordOld.getId() + ",\"requestedStatus\":\"PRESENT\",\"reason\":\"Old class\"}";
        mvc.perform(json(post("/api/v1/corrections"), body).cookie(studentCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SAMS-COR-400"));
    }

    @Test
    void studentViewsOnlyOwnCorrections() throws Exception {
        corrections.save(new CorrectionRequest(recordRecent, studentUser, AttendanceStatus.PRESENT, "Medical issue"));
        corrections.save(new CorrectionRequest(otherStudentRecord, otherStudentUser, AttendanceStatus.OD, "Sports"));

        mvc.perform(get("/api/v1/corrections/mine").cookie(studentCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].studentId").value("PES1UG24CS001"));
    }

    @Test
    void pendingPermissionsForFacultyAndAdmin() throws Exception {
        corrections.save(new CorrectionRequest(recordRecent, studentUser, AttendanceStatus.PRESENT, "Medical issue"));

        // Assigned faculty sees 1 pending
        mvc.perform(get("/api/v1/corrections/pending").cookie(assignedFaculty))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Unassigned faculty sees 0 pending
        mvc.perform(get("/api/v1/corrections/pending").cookie(otherFaculty))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Admin sees all pending
        mvc.perform(get("/api/v1/corrections/pending").cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Student cannot view pending
        mvc.perform(get("/api/v1/corrections/pending").cookie(studentCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignedFacultyApprovesCorrectionAndRecordIsUpdatedAndAuditLogged() throws Exception {
        CorrectionRequest req = corrections.save(new CorrectionRequest(recordRecent, studentUser, AttendanceStatus.PRESENT, "Medical cert"));

        String body = "{\"status\":\"APPROVED\",\"decisionNote\":\"Medical cert verified\"}";
        mvc.perform(json(patch("/api/v1/corrections/" + req.getId()), body).cookie(assignedFaculty))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.decisionNote").value("Medical cert verified"));

        // Verify attendance record updated
        AttendanceRecord updated = attendance.findById(recordRecent.getId()).orElseThrow();
        assertEquals(AttendanceStatus.PRESENT, updated.getStatus());

        // Verify audit log has ATTENDANCE_CORRECTED and CORRECTION_DECIDED
        assertTrue(audit.findAll().stream().anyMatch(a -> "ATTENDANCE_CORRECTED".equals(a.getAction())));
        assertTrue(audit.findAll().stream().anyMatch(a -> "CORRECTION_DECIDED".equals(a.getAction())));

        // Cannot decide again
        mvc.perform(json(patch("/api/v1/corrections/" + req.getId()), body).cookie(assignedFaculty))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SAMS-COR-409"));
    }

    @Test
    void unassignedFacultyCannotDecideCorrection() throws Exception {
        CorrectionRequest req = corrections.save(new CorrectionRequest(recordRecent, studentUser, AttendanceStatus.PRESENT, "Medical cert"));

        String body = "{\"status\":\"APPROVED\",\"decisionNote\":\"Approved\"}";
        mvc.perform(json(patch("/api/v1/corrections/" + req.getId()), body).cookie(otherFaculty))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SAMS-AUTH-403"));
    }

    @Test
    void adminCanDecideCorrection() throws Exception {
        CorrectionRequest req = corrections.save(new CorrectionRequest(recordRecent, studentUser, AttendanceStatus.PRESENT, "Medical cert"));

        String body = "{\"status\":\"REJECTED\",\"decisionNote\":\"Rejected by admin\"}";
        mvc.perform(json(patch("/api/v1/corrections/" + req.getId()), body).cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }
}
