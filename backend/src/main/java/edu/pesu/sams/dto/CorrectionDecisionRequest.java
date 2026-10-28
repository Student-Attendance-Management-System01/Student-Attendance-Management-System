package edu.pesu.sams.dto;
import edu.pesu.sams.entity.CorrectionStatus;
import jakarta.validation.constraints.*;
public record CorrectionDecisionRequest(@NotNull CorrectionStatus status, @Size(max=250) String decisionNote) {}
