package ma.emi.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CourseRequestDto(
        @NotBlank(message = "Course's name is compulsory") String name,
        @NotBlank(message = "Course's description is compulsory") String description,
        @NotBlank(message = "Course's code is compulsory") String code,
        @NotNull(message = "Course's capacity is compulsory") @Positive Long capacity
) {}
