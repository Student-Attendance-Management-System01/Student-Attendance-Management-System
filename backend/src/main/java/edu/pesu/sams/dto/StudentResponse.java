package edu.pesu.sams.dto;
import edu.pesu.sams.entity.UserAccount;
public record StudentResponse(Long id, String institutionId, String fullName, String email, boolean active, String createdAt) {
 public static StudentResponse of(UserAccount u){return new StudentResponse(u.getId(),u.getInstitutionId(),u.getFullName(),u.getEmail(),u.isActive(),String.valueOf(u.getCreatedAt()));}
}
