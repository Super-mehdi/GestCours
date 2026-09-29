package ma.emi.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import ma.emi.backend.entity.RegistrationStatus;

public record RegistrationRequestDto(
        @NotNull( message = "Student id is missing") Long studentId,
        @NotNull( message = "Course id is missing") Long courseId,
        RegistrationStatus registrationStatus
) {}
