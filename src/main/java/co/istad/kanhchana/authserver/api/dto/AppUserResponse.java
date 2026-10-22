package co.istad.kanhchana.authserver.api.dto;




public record AppUserResponse(
        Long id,
        String username,
        String email,
        String message
) {
}
