package edu.pesu.sams.service;

import edu.pesu.sams.exception.SamsException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {
 private final PasswordPolicy policy=new PasswordPolicy();
 @Test void acceptsPasswordWithEveryRequiredCharacterClass(){assertDoesNotThrow(()->policy.validate("StrongPass9!"));}
 @Test void rejectsPasswordWithoutUppercase(){assertThrows(SamsException.class,()->policy.validate("strongpass9!"));}
 @Test void rejectsPasswordWithoutNumber(){assertThrows(SamsException.class,()->policy.validate("StrongPass!"));}
 @Test void rejectsPasswordLongerThanBcryptLimit(){assertThrows(SamsException.class,()->policy.validate("A".repeat(65)+"a1!"));}
}
