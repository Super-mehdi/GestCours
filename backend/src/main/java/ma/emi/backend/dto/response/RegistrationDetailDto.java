package ma.emi.backend.dto.response;

import ma.emi.backend.entity.RegistrationStatus;

public record RegistrationDetailDto(
        Long id,
        Long studentId,
        String studentName,
        String studentEmail,
        Long courseId,
        String courseCode,
        String courseName,
        RegistrationStatus status
) {}
