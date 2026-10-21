
package co.istad.kanhchana.authserver.oauth2.service;

import co.istad.kanhchana.authserver.oauth2.entity.Client;
import co.istad.kanhchana.authserver.oauth2.repository.ClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class JpaRegisteredClientRepository
        implements RegisteredClientRepository {

    private final ClientRepository clientRepository;

    public JpaRegisteredClientRepository(
            ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RegisteredClient findById(String id) {
        return clientRepository.findById(id)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Client entity = clientRepository
                .findById(registeredClient.getId())
                .orElseGet(Client::new);

        entity.setId(registeredClient.getId());
        entity.setClientId(registeredClient.getClientId());
        entity.setClientIdIssuedAt(
                registeredClient.getClientIdIssuedAt());
        entity.setClientSecret(
                registeredClient.getClientSecret());
        entity.setClientSecretExpiresAt(
                registeredClient.getClientSecretExpiresAt());
        entity.setClientName(registeredClient.getClientName());

        entity.setClientAuthenticationMethods(
                registeredClient.getClientAuthenticationMethods()
                        .stream()
                        .map(ClientAuthenticationMethod::getValue)
                        .collect(Collectors.joining(",")));

        entity.setAuthorizationGrantTypes(
                registeredClient.getAuthorizationGrantTypes()
                        .stream()
                        .map(AuthorizationGrantType::getValue)
                        .collect(Collectors.joining(",")));

        entity.setRedirectUris(
                String.join(",", registeredClient.getRedirectUris()));

        entity.setPostLogoutRedirectUris(
                String.join(",",
                        registeredClient.getPostLogoutRedirectUris()));

        entity.setScopes(
                String.join(",", registeredClient.getScopes()));

        entity.setClientSettings(
                registeredClient.getClientSettings().getSettings()
                        .entrySet()
                        .stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(";")));

        entity.setTokenSettings(
                registeredClient.getTokenSettings().getSettings()
                        .entrySet()
                        .stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(";")));

        clientRepository.save(entity);
    }

    private RegisteredClient toRegisteredClient(Client entity) {
        RegisteredClient.Builder builder = RegisteredClient
                .withId(entity.getId())
                .clientId(entity.getClientId())
                .clientName(entity.getClientName());

        if (entity.getClientIdIssuedAt() != null) {
            builder.clientIdIssuedAt(entity.getClientIdIssuedAt());
        }

        if (entity.getClientSecret() != null) {
            builder.clientSecret(entity.getClientSecret());
        }

        if (entity.getClientSecretExpiresAt() != null) {
            builder.clientSecretExpiresAt(
                    entity.getClientSecretExpiresAt());
        }

        split(entity.getClientAuthenticationMethods())
                .forEach(value -> builder.clientAuthenticationMethod(
                        new ClientAuthenticationMethod(value)));

        split(entity.getAuthorizationGrantTypes())
                .forEach(value -> builder.authorizationGrantType(
                        new AuthorizationGrantType(value)));

        split(entity.getRedirectUris())
                .forEach(builder::redirectUri);

        split(entity.getPostLogoutRedirectUris())
                .forEach(builder::postLogoutRedirectUri);

        split(entity.getScopes())
                .forEach(builder::scope);

        builder.clientSettings(ClientSettings.builder().build());

        builder.tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(5))
                .refreshTokenTimeToLive(Duration.ofHours(8))
                .build());

        return builder.build();
    }

    private static java.util.List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return java.util.Collections.emptyList();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
