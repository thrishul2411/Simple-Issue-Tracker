package com.sit.simpleissuetracker.repository;

import com.sit.simpleissuetracker.modals.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

}
