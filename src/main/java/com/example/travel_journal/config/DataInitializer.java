package com.example.travel_journal.config;

import com.example.travel_journal.entity.Permission;
import com.example.travel_journal.entity.Role;
import com.example.travel_journal.repository.PermissionRepository;
import com.example.travel_journal.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 如果角色已存在，不進行初始化
        if (roleRepository.count() > 0) {
            return;
        }

        // 創建基本權限
        Permission readUser = createPermission("user:read", "Can read user info");
        Permission readAttraction = createPermission("attraction:read", "Can read attractions");
        Permission writeJournal = createPermission("journal:write", "Can write journals");
        Permission readJournal = createPermission("journal:read", "Can read journals");
        Permission readPoint = createPermission("point:read", "Can read points");

        // 創建用戶角色
        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("Regular User");
        userRole.setPermissions(new HashSet<>(Arrays.asList(
            readUser, readAttraction, readJournal, writeJournal, readPoint
        )));
        roleRepository.save(userRole);
    }

    private Permission createPermission(String name, String description) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }
} 