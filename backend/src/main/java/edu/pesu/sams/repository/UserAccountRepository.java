package edu.pesu.sams.repository;
import edu.pesu.sams.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface UserAccountRepository extends JpaRepository<UserAccount,Long> {
 Optional<UserAccount> findByEmailIgnoreCase(String email);
 Optional<UserAccount> findByInstitutionIdIgnoreCase(String institutionId);
 boolean existsByEmailIgnoreCase(String email);
 boolean existsByInstitutionIdIgnoreCase(String institutionId);
 List<UserAccount> findByRoleAndActiveTrue(UserRole role);
 List<UserAccount> findByRole(UserRole role);
 /** {@code pattern} is a lower-case LIKE pattern matched against institution ID, name and e-mail; {@code active} null means any status. */
 @Query("select u from UserAccount u where u.role=:role and (:active is null or u.active=:active) and (lower(u.institutionId) like :pattern escape '\\' or lower(u.fullName) like :pattern escape '\\' or lower(u.email) like :pattern escape '\\')")
 Page<UserAccount> search(@Param("role") UserRole role,@Param("active") Boolean active,@Param("pattern") String pattern,Pageable pageable);
}
