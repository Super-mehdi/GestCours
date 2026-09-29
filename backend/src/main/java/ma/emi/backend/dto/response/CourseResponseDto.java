package ma.emi.backend.dto.response;

public record CourseResponseDto(
        Long id,
        String name,
        String code,
        String description,
        Long capacity
) {}
