package ma.emi.backend.dto.response;

import ma.emi.backend.entity.Role;

public record LoginResponseDto(
        String token,
        String email,
        Role role,
        Long studentId,
        String studentName
) {}
