package com.sit.simpleissuetracker.repository;

import com.sit.simpleissuetracker.modals.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public Optional<User> findByEmail(String email);
    public Boolean existsByEmail(String email);
}
