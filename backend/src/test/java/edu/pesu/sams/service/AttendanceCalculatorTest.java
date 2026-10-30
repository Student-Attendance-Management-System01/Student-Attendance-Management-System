package edu.pesu.sams.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AttendanceCalculatorTest {
 private final AttendanceCalculator calculator=new AttendanceCalculator();
 @Test void countsOnDutyAsAttendedAndCalculatesExactPercentage(){assertEquals(new BigDecimal("75.00"),calculator.percentage(3,4));}
 @Test void roundsHalfUpToTwoDecimalPlaces(){assertEquals(new BigDecimal("66.67"),calculator.percentage(2,3));}
 @Test void returnsZeroWhenNoSessionsHaveBeenHeld(){assertEquals(new BigDecimal("0.00"),calculator.percentage(0,0));}
 @Test void doesNotReturnMoreThanOneHundredForValidCounts(){assertEquals(new BigDecimal("100.00"),calculator.percentage(2,2));}
}
