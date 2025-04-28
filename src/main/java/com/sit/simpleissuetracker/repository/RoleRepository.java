package com.sit.simpleissuetracker.repository;

import com.sit.simpleissuetracker.modals.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
}
