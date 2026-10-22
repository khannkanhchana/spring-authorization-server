package co.istad.kanhchana.authserver.appuser;




import co.istad.kanhchana.authserver.api.dto.AppUserResponse;
import co.istad.kanhchana.authserver.api.dto.CreateAppUserRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUserResponse createAppUser(
            CreateAppUserRequest request) {

        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        if (appUserRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Email already exists"
            );
        }

        AppUser appUser = new AppUser(
                request.username(),
                request.email()
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
