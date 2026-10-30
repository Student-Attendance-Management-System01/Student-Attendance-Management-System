package edu.pesu.sams.dto;
import edu.pesu.sams.entity.SessionType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
public record CreateSessionRequest(@NotNull Long courseSectionId, @NotNull LocalDate date, @NotNull LocalTime startTime, @NotNull LocalTime endTime, @NotNull SessionType type) {}
