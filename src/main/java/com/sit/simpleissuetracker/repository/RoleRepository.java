package com.sit.simpleissuetracker.repository;

import com.sit.simpleissuetracker.modals.Role;
import com.sit.simpleissuetracker.modals.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    public Optional<Role> findByName(RoleName name);
}
