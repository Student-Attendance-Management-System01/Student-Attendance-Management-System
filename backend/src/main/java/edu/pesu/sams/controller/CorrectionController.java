package edu.pesu.sams.controller;

import edu.pesu.sams.dto.*;
import edu.pesu.sams.entity.*;
import edu.pesu.sams.service.CorrectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/v1/corrections")
public class CorrectionController {
 private final CorrectionService corrections; private final edu.pesu.sams.service.UserManagementService management;
 public CorrectionController(CorrectionService corrections,edu.pesu.sams.service.UserManagementService management){this.corrections=corrections;this.management=management;}
 @PostMapping @PreAuthorize("hasRole('STUDENT')") public ResponseEntity<Map<String,Object>> create(@Valid @RequestBody CorrectionRequestCreate request,Authentication auth,HttpServletRequest req){return ResponseEntity.status(HttpStatus.CREATED).body(corrections.create(request,auth.getName(),req));}
 @GetMapping("/mine") @PreAuthorize("hasRole('STUDENT')") public List<Map<String,Object>> mine(Authentication auth){return corrections.mine(auth.getName());}
 @GetMapping("/pending") @PreAuthorize("hasAnyRole('FACULTY','ADMIN')") public List<Map<String,Object>> pending(Authentication auth){UserAccount u=management.requireUser(auth.getName());return corrections.pending(auth.getName(),u.getRole());}
 @PatchMapping("/{id}") @PreAuthorize("hasRole('FACULTY')") public Map<String,Object> decide(@PathVariable Long id,@Valid @RequestBody CorrectionDecisionRequest request,Authentication auth,HttpServletRequest req){return corrections.decide(id,request,auth.getName(),req);}
}
