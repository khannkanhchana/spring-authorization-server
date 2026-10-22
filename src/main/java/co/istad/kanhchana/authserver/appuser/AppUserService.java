
package co.istad.kanhchana.authserver.appuser;

import co.istad.kanhchana.authserver.api.dto.AppUserResponse;
import co.istad.kanhchana.authserver.api.dto.CreateAppUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUserResponse createAppUser(CreateAppUserRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }

        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (appUserRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        String encodedPassword =
                passwordEncoder.encode(request.password());

        AppUser appUser = new AppUser(
                request.username(),
                request.email(),
                encodedPassword
        );

        AppUser savedUser = appUserRepository.save(appUser);

        return new AppUserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                "User saved to PostgreSQL successfully"
        );
    }
}
