package edu.pesu.sams.service;
import edu.pesu.sams.exception.SamsException;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;
@Component
public class PasswordPolicy {
 private static final Pattern RULE=Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$");
 public void validate(String password){if(password==null||!RULE.matcher(password).matches())throw SamsException.badRequest("Password must be 8–64 characters and include an uppercase letter, a lowercase letter, a number and a special character");}
}
