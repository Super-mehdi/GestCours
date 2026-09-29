package ma.emi.backend.dto.response;

import ma.emi.backend.entity.RegistrationStatus;

public record RegistrationResponseDto(
        Long id,
        Long studentId,
        Long courseId,
        RegistrationStatus registrationStatus
) {}
