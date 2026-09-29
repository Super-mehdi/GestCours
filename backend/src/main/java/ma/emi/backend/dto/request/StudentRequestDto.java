package ma.emi.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StudentRequestDto(
        @NotBlank(message = "Student's first name is compulsory") String firstName,
        @NotBlank(message = "Student's last name is compulsory") String lastName,
        @NotBlank(message = "Student's email cannot be blank") @Email(message = "Invalid email") String email
) {}
