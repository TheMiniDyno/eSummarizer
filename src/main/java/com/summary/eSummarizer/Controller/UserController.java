package com.summary.eSummarizer.Controller;

import com.summary.eSummarizer.Model.MyAppUser;
import com.summary.eSummarizer.Model.MyAppUserService;
import com.summary.eSummarizer.Service.UserOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class UserController {

    @Autowired
    private MyAppUserService userService;

    @Autowired
    private UserOperationService userOperationService;

    @GetMapping
    public ResponseEntity<MyAppUser> getUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MyAppUser user = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/username")
    public ResponseEntity<?> updateUsername(@RequestParam String newUsername) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            userService.updateUsername(auth.getName(), newUsername);
            return ResponseEntity.ok().body("Username updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            userOperationService.changePassword(auth.getName(), oldPassword, newPassword);
            return ResponseEntity.ok().body("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}