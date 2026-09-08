package com.hospital.hms.service;

import com.hospital.hms.entity.User;

import java.util.List;

public interface UserService {

    User getUserById(Long id);

    User getUserByUsername(String username);

    List<User> getAllUsers();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
