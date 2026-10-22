package co.istad.kanhchana.authserver.api;



import co.istad.kanhchana.authserver.api.dto.AppUserResponse;
import co.istad.kanhchana.authserver.api.dto.CreateAppUserRequest;
import co.istad.kanhchana.authserver.appuser.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/app-users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping
    public ResponseEntity<AppUserResponse> createAppUser(
            @RequestBody CreateAppUserRequest request) {

        AppUserResponse response =
                appUserService.createAppUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
