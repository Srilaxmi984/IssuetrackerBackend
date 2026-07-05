package com.issuetracker.service;

import com.issuetracker.dto.LoginRequest;
import com.issuetracker.dto.LoginResponse;
import com.issuetracker.dto.RegisterRequest;
import com.issuetracker.enums.Role;
import com.issuetracker.exception.BadRequestException;
import com.issuetracker.exception.ResourceNotFoundException;
import com.issuetracker.model.User;
import com.issuetracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Auto-create the single ADMIN user on startup if not present
    @Bean
    public CommandLineRunner seedAdmin() {
        return args -> {
            if (!userRepository.existsByRole(Role.ADMIN)) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@issuetracker.com");
                admin.setPassword("admin123");
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                System.out.println("===========================================");
                System.out.println("  ADMIN created: admin@issuetracker.com");
                System.out.println("  Password: admin123");
                System.out.println("===========================================");
            }
        };
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }
        return new LoginResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    public List<User> getAllUsers() { return userRepository.findAll(); }

    public List<User> getByRole(String role) {
        return userRepository.findByRole(Role.valueOf(role.toUpperCase()));
    }

    public List<User> getManagers() { return userRepository.findByRole(Role.MANAGER); }
    public List<User> getDevelopers() { return userRepository.findByRole(Role.DEVELOPER); }
    public List<User> getReporters() { return userRepository.findByRole(Role.REPORTER); }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public User createUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new BadRequestException("Email already in use");
        if (userRepository.existsByUsername(request.getUsername()))
            throw new BadRequestException("Username already taken");
        // Prevent creating another ADMIN
        if ("ADMIN".equalsIgnoreCase(request.getRole()))
            throw new BadRequestException("Cannot create additional admin users");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        return userRepository.save(user);
    }

    public User updateUser(Long id, RegisterRequest request) {
        User user = getUserById(id);
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty())
            user.setPassword(request.getPassword());
        if (request.getRole() != null && !"ADMIN".equalsIgnoreCase(request.getRole()))
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        if (user.getRole() == Role.ADMIN)
            throw new BadRequestException("Cannot delete admin user");
        userRepository.deleteById(id);
    }
}
