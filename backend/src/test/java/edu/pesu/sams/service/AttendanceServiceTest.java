package edu.pesu.sams.service;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.exception.SamsException;
import edu.pesu.sams.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sams-attendance-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "management.health.mail.enabled=false",
        "sams.jwt.secret=c2Ftcy1pbnRlZ3JhdGlvbi10ZXN0LXNlY3JldC1rZXktMDEyMzQ1Njc4OQ=="
})
class AttendanceServiceTest {
    @Autowired AttendanceService attendanceService;
    @Autowired UserAccountRepository users;
    @Autowired CourseSectionRepository sections;
    @Autowired EnrollmentRepository enrolments;
    @Autowired ClassSessionRepository sessions;
    @Autowired AttendanceRecordRepository records;

    private UserAccount adminUser, facultyUser, otherFaculty, student1, student2;
    private CourseSection section;

    @BeforeEach
    void setup() {
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

        otherFaculty = new UserAccount();
        otherFaculty.setInstitutionId("EMP002");
        otherFaculty.setFullName("Faculty Two");
        otherFaculty.setEmail("faculty2@example.edu");
        otherFaculty.setRole(UserRole.FACULTY);
        otherFaculty.setPasswordHash("$2a$12$hash");
        otherFaculty.setActive(true);
        users.save(otherFaculty);

        student1 = new UserAccount();
        student1.setInstitutionId("PES1UG24CS101");
        student1.setFullName("Student One");
        student1.setEmail("student1@example.edu");
        student1.setRole(UserRole.STUDENT);
        student1.setPasswordHash("$2a$12$hash");
        student1.setActive(true);
        users.save(student1);

        student2 = new UserAccount();
        student2.setInstitutionId("PES1UG24CS102");
        student2.setFullName("Student Two");
        student2.setEmail("student2@example.edu");
        student2.setRole(UserRole.STUDENT);
        student2.setPasswordHash("$2a$12$hash");
        student2.setActive(true);
        users.save(student2);

        section = new CourseSection();
        section.setCourseCode("CS301");
        section.setCourseName("Operating Systems");
        section.setSemester("5");
        section.setSection("A");
        section.setAcademicYear("2026-27");
        section.setActive(true);
        section.setFaculty(new HashSet<>(List.of(facultyUser)));
        sections.save(section);

        enrolments.save(new Enrollment(section, student1));
        enrolments.save(new Enrollment(section, student2));
    }

    @Test
    void createSessionAndOverlapCheck() {
        CreateSessionRequest req1 = new CreateSessionRequest(
                section.getId(),
                LocalDate.of(2026, 11, 25),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                SessionType.LECTURE
        );
        Map<String, Object> sess = attendanceService.createSession(facultyUser.getEmail(), req1, null);
        assertNotNull(sess.get("id"));

        // Overlapping session should throw conflict
        CreateSessionRequest req2 = new CreateSessionRequest(
                section.getId(),
                LocalDate.of(2026, 11, 25),
                LocalTime.of(9, 30),
                LocalTime.of(10, 30),
                SessionType.LECTURE
        );
        assertThrows(SamsException.class, () -> attendanceService.createSession(facultyUser.getEmail(), req2, null));
    }

    @Test
    void submitAndRetrieveAttendance() {
        CreateSessionRequest req1 = new CreateSessionRequest(
                section.getId(),
                LocalDate.of(2026, 11, 26),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                SessionType.LECTURE
        );
        Map<String, Object> sess = attendanceService.createSession(facultyUser.getEmail(), req1, null);
        Long sessId = (Long) sess.get("id");

        // Roster before submission
        Map<String, Object> roster = attendanceService.roster(sessId, facultyUser.getEmail());
        assertEquals(2, ((List<?>) roster.get("students")).size());

        // Submit attendance
        AttendanceSubmissionRequest subReq = new AttendanceSubmissionRequest(List.of(
                new AttendanceSubmissionRequest.AttendanceMark(student1.getInstitutionId(), AttendanceStatus.PRESENT),
                new AttendanceSubmissionRequest.AttendanceMark(student2.getInstitutionId(), AttendanceStatus.ABSENT)
        ));
        Map<String, Object> subResult = attendanceService.submitAttendance(sessId, subReq, facultyUser.getEmail(), null);
        assertEquals(1, subResult.get("present"));
        assertEquals(1, subResult.get("absent"));

        // Duplicate submission should fail
        assertThrows(SamsException.class, () -> attendanceService.submitAttendance(sessId, subReq, facultyUser.getEmail(), null));

        // Edit attendance
        EditAttendanceRequest editReq = new EditAttendanceRequest(AttendanceStatus.PRESENT, "Late bus approved");
        Map<String, Object> editResult = attendanceService.editAttendance(sessId, student2.getInstitutionId(), editReq, facultyUser.getEmail(), UserRole.FACULTY, null);
        assertEquals("PRESENT", editResult.get("newStatus"));

        // Student own summary
        Map<String, Object> ownSummary = attendanceService.ownSummary(student1.getInstitutionId(), student1.getEmail());
        assertEquals(student1.getInstitutionId(), ownSummary.get("studentId"));

        // Section attendance
        Map<String, Object> secAtt = attendanceService.sectionAttendance(section.getId(), facultyUser.getEmail(), UserRole.FACULTY);
        assertNotNull(secAtt.get("courseSection"));
    }

    @Test
    void unassignedFacultyCannotCreateOrSubmit() {
        CreateSessionRequest req1 = new CreateSessionRequest(
                section.getId(),
                LocalDate.of(2026, 11, 27),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                SessionType.LECTURE
        );
        assertThrows(SamsException.class, () -> attendanceService.createSession(otherFaculty.getEmail(), req1, null));
    }
}
