package ma.emi.backend.dto.response;

public record StudentResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email
) {}

