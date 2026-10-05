package ma.emi.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.emi.backend.dto.request.LoginRequestDto;
import ma.emi.backend.dto.response.LoginResponseDto;
import ma.emi.backend.dto.response.TestCredentialDto;
import ma.emi.backend.entity.Role;
import ma.emi.backend.entity.User;
import ma.emi.backend.repository.UserRepository;
import ma.emi.backend.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email().trim())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password.");
        }

        Long studentId = user.getStudent() != null ? user.getStudent().getId() : null;
        String studentName = user.getStudent() != null
                ? user.getStudent().getFirstName() + " " + user.getStudent().getLastName()
                : (user.getRole() == Role.ROLE_ADMIN ? "Administrator" : "User");

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), studentId);

        return ResponseEntity.ok(new LoginResponseDto(
                token,
                user.getEmail(),
                user.getRole(),
                studentId,
                studentName
        ));
    }

    @GetMapping("/test-credentials")
    public List<TestCredentialDto> getTestCredentials() {
        return List.of(
                new TestCredentialDto(
                        "Admin Account",
                        "admin@portal.com",
                        "admin123",
                        "ROLE_ADMIN",
                        "Full administrative access: Student/Course management & Course request approval"
                ),
                new TestCredentialDto(
                        "Student (Youssef)",
                        "youssef.elamrani@example.com",
                        "student123",
                        "ROLE_STUDENT",
                        "Pre-seeded student account: view assigned courses & request new enrollments"
                ),
                new TestCredentialDto(
                        "Student (Sara)",
                        "sara.benali@example.com",
                        "student123",
                        "ROLE_STUDENT",
                        "Pre-seeded student account: view assigned courses & request new enrollments"
                )
        );
    }
}
