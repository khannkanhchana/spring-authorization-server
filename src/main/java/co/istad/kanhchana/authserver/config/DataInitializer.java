
package co.istad.kanhchana.authserver.config;

import co.istad.kanhchana.authserver.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeOAuth2Client(
            RegisteredClientRepository registeredClientRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String clientId = "postman-client";

            if (registeredClientRepository.findByClientId(clientId) != null) {
                return;
            }

            RegisteredClient client = RegisteredClient.withId(
                            UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientSecret(passwordEncoder.encode("secret"))
                    .clientName("Postman OAuth2 Client")

                    // Client Authentication
                    .clientAuthenticationMethod(
                            ClientAuthenticationMethod.CLIENT_SECRET_BASIC)

                    // OAuth2 Grant Types
                    .authorizationGrantType(
                            AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(
                            AuthorizationGrantType.REFRESH_TOKEN)

                    // Postman OAuth2 Callback URL
                    .redirectUri(
                            "https://oauth.pstmn.io/v1/callback")

                    // OAuth2 Scopes
                    .scope("openid")
                    .scope("profile")
                    .scope("read")

                    // Client Settings
                    .clientSettings(
                            ClientSettings.builder()
                                    .requireAuthorizationConsent(true)
                                    .requireProofKey(true)
                                    .build())

                    // Token Settings
                    .tokenSettings(
                            TokenSettings.builder()
                                    .accessTokenTimeToLive(
                                            Duration.ofMinutes(15))
                                    .refreshTokenTimeToLive(
                                            Duration.ofHours(8))
                                    .reuseRefreshTokens(false)
                                    .build())

                    .build();

            registeredClientRepository.save(client);

            System.out.println(
                    "OAuth2 Client initialized: " + clientId);
        };
    }


    @Bean
    public CommandLineRunner initializeDefaultUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String username = "user";

            if (userRepository.existsByUsername(username)) {
                return;
            }

            co.istad.kanhchana.authserver.user.User user =
                    new co.istad.kanhchana.authserver.user.User(
                            username,
                            passwordEncoder.encode("1234"),
                            "user@example.com",
                            "USER"
                    );

            userRepository.save(user);

            System.out.println("Default user created: " + username);
        };
    }

}
