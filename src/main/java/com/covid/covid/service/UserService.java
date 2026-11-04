package com.covid.covid.service;

import com.covid.covid.model.User;
import com.covid.covid.repository.Userrepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Userrepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(Userrepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String signup(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()) {
            return "Email and password are required";
        }

        String username = normalizeUsername(user.getUsername());
        if (userRepository.findByUsername(username) != null) {
            return "Email is already registered";
        }

        User newUser = new User(username, passwordEncoder.encode(user.getPassword()));
        userRepository.save(newUser);
        return "Signup successful";
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
