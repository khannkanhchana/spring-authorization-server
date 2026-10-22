package co.istad.kanhchana.authserver.api.dto;



public record CreateAppUserRequest(
        String username,
        String email,
        String password
) {
}
