package com.example.travel_journal.repository;

import com.example.travel_journal.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
} 