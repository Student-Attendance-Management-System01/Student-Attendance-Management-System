package edu.pesu.sams.service;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Single source of truth for SAMS-NF-012: OD counts as attended; rounded half-up to two decimals. */
@Component
public class AttendanceCalculator {
 public BigDecimal percentage(long attended,long held){
   if(held<=0)return BigDecimal.ZERO.setScale(2,RoundingMode.HALF_UP);
   return BigDecimal.valueOf(attended).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(held),2,RoundingMode.HALF_UP);
 }
}
