package com.example.travel_journal.service;

import com.example.travel_journal.entity.Role;
import com.example.travel_journal.entity.User;
import com.example.travel_journal.repository.RoleRepository;
import com.example.travel_journal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public User findOrCreateUser(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(email);
                    newUser.setFullName(name);
                    newUser.setEnabled(true);
                    
                    // 為 OAuth 用戶生成隨機密碼
                    String randomPassword = UUID.randomUUID().toString();
                    newUser.setPassword(passwordEncoder.encode(randomPassword));

                    // 設置預設角色為 USER
                    roleRepository.findByName("USER").ifPresent(role -> 
                        newUser.getRoles().add(role)
                    );

                    return userRepository.save(newUser);
                });
    }
} 