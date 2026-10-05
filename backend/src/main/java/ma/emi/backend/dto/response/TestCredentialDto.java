package ma.emi.backend.dto.response;

public record TestCredentialDto(
        String label,
        String email,
        String password,
        String role,
        String description
) {}
