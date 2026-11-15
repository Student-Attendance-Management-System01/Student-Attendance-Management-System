package edu.pesu.sams.controller;

import edu.pesu.sams.dto.RosterResponse;
import edu.pesu.sams.service.RosterService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1")
public class RosterController {
 private final RosterService rosters;
 public RosterController(RosterService rosters){this.rosters=rosters;}
 @GetMapping("/course-sections/{id}/roster") @PreAuthorize("hasAnyRole('FACULTY','ADMIN')") public RosterResponse roster(@PathVariable Long id,Authentication auth){return rosters.roster(id,auth.getName());}
}
