package com.summary.eSummarizer.Service;

import com.summary.eSummarizer.Model.MyAppUser;
import com.summary.eSummarizer.Model.MyAppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserOperationService {

    private static final Logger logger = LoggerFactory.getLogger(UserOperationService.class);

    @Autowired
    private MyAppUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void changePassword(String email, String oldPassword, String newPassword) {
        MyAppUser user = userService.findByEmail(email);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }
        String encodedPassword = passwordEncoder.encode(newPassword);
        userService.updatePassword(email, encodedPassword);
    }
}
