package com.summary.eSummarizer.Service;

import com.summary.eSummarizer.Model.UserModel;
import com.summary.eSummarizer.Repository.MyAppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyAppUserService implements UserDetailsService {

    @Autowired
    private MyAppUserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserModel user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public UserModel findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public UserModel findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public void updateUsername(String email, String newUsername) {
        UserModel user = findByEmail(email);
        user.setUsername(newUsername);
        repository.save(user);
    }

    public void updatePassword(String email, String newPassword) {
        UserModel user = findByEmail(email);
        user.setPassword(newPassword);
        repository.save(user);
    }
}