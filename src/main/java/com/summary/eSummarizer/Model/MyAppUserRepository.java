package com.summary.eSummarizer.Model;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyAppUserRepository extends JpaRepository<MyAppUser, Long> {

    List<MyAppUser> findByUsername(String username);  // Updated to return a List

    Optional<MyAppUser> findByEmail(String email);
}
