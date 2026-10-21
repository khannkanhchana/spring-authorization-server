
package co.istad.kanhchana.authserver.oauth2.service;

import co.istad.kanhchana.authserver.oauth2.entity.AuthorizationConsent;
import co.istad.kanhchana.authserver.oauth2.repository.AuthorizationConsentRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class JpaOAuth2AuthorizationConsentService
        implements OAuth2AuthorizationConsentService {

    private final AuthorizationConsentRepository consentRepository;
    private final RegisteredClientRepository registeredClientRepository;

    public JpaOAuth2AuthorizationConsentService(
            AuthorizationConsentRepository consentRepository,
            RegisteredClientRepository registeredClientRepository) {
        Assert.notNull(consentRepository,
                "consentRepository cannot be null");
        Assert.notNull(registeredClientRepository,
                "registeredClientRepository cannot be null");

        this.consentRepository = consentRepository;
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2AuthorizationConsent findById(
            String registeredClientId,
            String principalName) {

        Assert.hasText(registeredClientId,
                "registeredClientId cannot be empty");
        Assert.hasText(principalName,
                "principalName cannot be empty");

        return consentRepository
                .findByRegisteredClientIdAndPrincipalName(
                        registeredClientId,
                        principalName)
                .map(this::toObject)
                .orElse(null);
    }

    @Override
    public void save(OAuth2AuthorizationConsent consent) {
        Assert.notNull(consent, "consent cannot be null");

        AuthorizationConsent entity = new AuthorizationConsent();
        entity.setRegisteredClientId(
                consent.getRegisteredClientId());
        entity.setPrincipalName(consent.getPrincipalName());

        String authorities = consent.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(","));

        entity.setAuthorities(authorities);

        consentRepository.save(entity);
    }

    @Override
    public void remove(OAuth2AuthorizationConsent consent) {
        Assert.notNull(consent, "consent cannot be null");

        consentRepository.deleteByRegisteredClientIdAndPrincipalName(
                consent.getRegisteredClientId(),
                consent.getPrincipalName());
    }

    private OAuth2AuthorizationConsent toObject(
            AuthorizationConsent entity) {

        RegisteredClient registeredClient =
                registeredClientRepository.findById(
                        entity.getRegisteredClientId());

        if (registeredClient == null) {
            throw new IllegalStateException(
                    "RegisteredClient not found: "
                            + entity.getRegisteredClientId());
        }

        OAuth2AuthorizationConsent.Builder builder =
                OAuth2AuthorizationConsent.withId(
                        registeredClient.getId(),
                        entity.getPrincipalName());

        Set<String> authorities = split(entity.getAuthorities());

        authorities.forEach(authority ->
                builder.authority(
                        new SimpleGrantedAuthority(authority)));

        return builder.build();
    }

    private Set<String> split(String value) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptySet();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
}
