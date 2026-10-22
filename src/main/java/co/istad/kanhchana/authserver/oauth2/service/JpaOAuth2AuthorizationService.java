
package co.istad.kanhchana.authserver.oauth2.service;

import co.istad.kanhchana.authserver.oauth2.entity.Authorization;
import co.istad.kanhchana.authserver.oauth2.repository.AuthorizationRepository;
import co.istad.kanhchana.authserver.user.User;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2DeviceCode;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.OAuth2UserCode;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Service
@Transactional
public class JpaOAuth2AuthorizationService
        implements OAuth2AuthorizationService {

    private final AuthorizationRepository authorizationRepository;
    private final RegisteredClientRepository registeredClientRepository;
    private final JsonMapper objectMapper;

    public JpaOAuth2AuthorizationService(
            AuthorizationRepository authorizationRepository,
            RegisteredClientRepository registeredClientRepository) {

        Assert.notNull(
                authorizationRepository,
                "authorizationRepository cannot be null"
        );

        Assert.notNull(
                registeredClientRepository,
                "registeredClientRepository cannot be null"
        );

        this.authorizationRepository = authorizationRepository;
        this.registeredClientRepository = registeredClientRepository;

        ClassLoader classLoader =
                JpaOAuth2AuthorizationService.class.getClassLoader();

        BasicPolymorphicTypeValidator.Builder validator =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(User.class);

        this.objectMapper = JsonMapper.builder()
                .addModules(
                        SecurityJacksonModules.getModules(
                                classLoader,
                                validator
                        )
                )
                .build();
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");

        authorizationRepository.save(toEntity(authorization));
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");

        authorizationRepository.deleteById(authorization.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2Authorization findById(String id) {
        Assert.hasText(id, "id cannot be empty");

        return authorizationRepository.findById(id)
                .map(this::toObject)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public OAuth2Authorization findByToken(
            String token,
            OAuth2TokenType tokenType) {

        Assert.hasText(token, "token cannot be empty");

        if (tokenType == null) {
            return authorizationRepository.findByToken(token)
                    .map(this::toObject)
                    .orElse(null);
        }

        String type = tokenType.getValue();

        if (OAuth2TokenType.ACCESS_TOKEN.getValue().equals(type)) {
            return authorizationRepository.findByAccessTokenValue(token)
                    .map(this::toObject)
                    .orElse(null);
        }

        if (OAuth2TokenType.REFRESH_TOKEN.getValue().equals(type)) {
            return authorizationRepository.findByRefreshTokenValue(token)
                    .map(this::toObject)
                    .orElse(null);
        }

        if ("code".equals(type)) {
            return authorizationRepository
                    .findByAuthorizationCodeValue(token)
                    .map(this::toObject)
                    .orElse(null);
        }

        if ("id_token".equals(type)) {
            return authorizationRepository.findByOidcIdTokenValue(token)
                    .map(this::toObject)
                    .orElse(null);
        }

        if ("user_code".equals(type)) {
            return authorizationRepository.findByUserCodeValue(token)
                    .map(this::toObject)
                    .orElse(null);
        }

        if ("device_code".equals(type)) {
            return authorizationRepository.findByDeviceCodeValue(token)
                    .map(this::toObject)
                    .orElse(null);
        }

        if ("state".equals(type)) {
            return authorizationRepository.findByState(token)
                    .map(this::toObject)
                    .orElse(null);
        }

        return null;
    }

    private Authorization toEntity(OAuth2Authorization authorization) {
        Authorization entity = authorizationRepository
                .findById(authorization.getId())
                .orElseGet(Authorization::new);

        entity.setId(authorization.getId());
        entity.setRegisteredClientId(
                authorization.getRegisteredClientId()
        );
        entity.setPrincipalName(authorization.getPrincipalName());
        entity.setAuthorizationGrantType(
                authorization.getAuthorizationGrantType().getValue()
        );
        entity.setAuthorizedScopes(
                StringUtils.collectionToCommaDelimitedString(
                        authorization.getAuthorizedScopes()
                )
        );
        entity.setAttributes(writeMap(authorization.getAttributes()));
        entity.setState(
                authorization.getAttribute(OAuth2ParameterNames.STATE)
        );

        OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);

        setTokenValues(
                authorizationCode,
                entity::setAuthorizationCodeValue,
                entity::setAuthorizationCodeIssuedAt,
                entity::setAuthorizationCodeExpiresAt,
                entity::setAuthorizationCodeMetadata
        );

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken =
                authorization.getToken(OAuth2AccessToken.class);

        setTokenValues(
                accessToken,
                entity::setAccessTokenValue,
                entity::setAccessTokenIssuedAt,
                entity::setAccessTokenExpiresAt,
                entity::setAccessTokenMetadata
        );

        if (accessToken != null) {
            entity.setAccessTokenType(
                    accessToken.getToken().getTokenType().getValue()
            );
            entity.setAccessTokenScopes(
                    StringUtils.collectionToCommaDelimitedString(
                            accessToken.getToken().getScopes()
                    )
            );
        }

        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
                authorization.getToken(OAuth2RefreshToken.class);

        setTokenValues(
                refreshToken,
                entity::setRefreshTokenValue,
                entity::setRefreshTokenIssuedAt,
                entity::setRefreshTokenExpiresAt,
                entity::setRefreshTokenMetadata
        );

        OAuth2Authorization.Token<OidcIdToken> idToken =
                authorization.getToken(OidcIdToken.class);

        setTokenValues(
                idToken,
                entity::setOidcIdTokenValue,
                entity::setOidcIdTokenIssuedAt,
                entity::setOidcIdTokenExpiresAt,
                entity::setOidcIdTokenMetadata
        );

        if (idToken != null) {
            entity.setOidcIdTokenClaims(
                    writeMap(idToken.getToken().getClaims())
            );
        }

        OAuth2Authorization.Token<OAuth2UserCode> userCode =
                authorization.getToken(OAuth2UserCode.class);

        setTokenValues(
                userCode,
                entity::setUserCodeValue,
                entity::setUserCodeIssuedAt,
                entity::setUserCodeExpiresAt,
                entity::setUserCodeMetadata
        );

        OAuth2Authorization.Token<OAuth2DeviceCode> deviceCode =
                authorization.getToken(OAuth2DeviceCode.class);

        setTokenValues(
                deviceCode,
                entity::setDeviceCodeValue,
                entity::setDeviceCodeIssuedAt,
                entity::setDeviceCodeExpiresAt,
                entity::setDeviceCodeMetadata
        );

        return entity;
    }

    private OAuth2Authorization toObject(Authorization entity) {
        RegisteredClient registeredClient =
                registeredClientRepository.findById(
                        entity.getRegisteredClientId()
                );

        if (registeredClient == null) {
            throw new DataRetrievalFailureException(
                    "RegisteredClient with id '"
                            + entity.getRegisteredClientId()
                            + "' was not found."
            );
        }

        OAuth2Authorization.Builder builder =
                OAuth2Authorization.withRegisteredClient(registeredClient)
                        .id(entity.getId())
                        .principalName(entity.getPrincipalName())
                        .authorizationGrantType(
                                resolveAuthorizationGrantType(
                                        entity.getAuthorizationGrantType()
                                )
                        )
                        .authorizedScopes(
                                StringUtils.commaDelimitedListToSet(
                                        entity.getAuthorizedScopes() == null
                                                ? ""
                                                : entity.getAuthorizedScopes()
                                )
                        )
                        .attributes(attributes ->
                                attributes.putAll(
                                        parseMap(entity.getAttributes())
                                )
                        );

        if (entity.getState() != null) {
            builder.attribute(
                    OAuth2ParameterNames.STATE,
                    entity.getState()
            );
        }

        if (entity.getAuthorizationCodeValue() != null) {
            OAuth2AuthorizationCode code =
                    new OAuth2AuthorizationCode(
                            entity.getAuthorizationCodeValue(),
                            entity.getAuthorizationCodeIssuedAt(),
                            entity.getAuthorizationCodeExpiresAt()
                    );

            builder.token(
                    code,
                    metadata -> metadata.putAll(
                            parseMap(entity.getAuthorizationCodeMetadata())
                    )
            );
        }

        if (entity.getAccessTokenValue() != null) {
            Set<String> scopes =
                    StringUtils.commaDelimitedListToSet(
                            entity.getAccessTokenScopes() == null
                                    ? ""
                                    : entity.getAccessTokenScopes()
                    );

            OAuth2AccessToken accessToken =
                    new OAuth2AccessToken(
                            OAuth2AccessToken.TokenType.BEARER,
                            entity.getAccessTokenValue(),
                            entity.getAccessTokenIssuedAt(),
                            entity.getAccessTokenExpiresAt(),
                            scopes
                    );

            builder.token(
                    accessToken,
                    metadata -> metadata.putAll(
                            parseMap(entity.getAccessTokenMetadata())
                    )
            );
        }

        if (entity.getRefreshTokenValue() != null) {
            OAuth2RefreshToken refreshToken =
                    new OAuth2RefreshToken(
                            entity.getRefreshTokenValue(),
                            entity.getRefreshTokenIssuedAt(),
                            entity.getRefreshTokenExpiresAt()
                    );

            builder.token(
                    refreshToken,
                    metadata -> metadata.putAll(
                            parseMap(entity.getRefreshTokenMetadata())
                    )
            );
        }

        if (entity.getOidcIdTokenValue() != null) {
            OidcIdToken idToken =
                    new OidcIdToken(
                            entity.getOidcIdTokenValue(),
                            entity.getOidcIdTokenIssuedAt(),
                            entity.getOidcIdTokenExpiresAt(),
                            parseMap(entity.getOidcIdTokenClaims())
                    );

            builder.token(
                    idToken,
                    metadata -> metadata.putAll(
                            parseMap(entity.getOidcIdTokenMetadata())
                    )
            );
        }

        if (entity.getUserCodeValue() != null) {
            OAuth2UserCode userCode =
                    new OAuth2UserCode(
                            entity.getUserCodeValue(),
                            entity.getUserCodeIssuedAt(),
                            entity.getUserCodeExpiresAt()
                    );

            builder.token(
                    userCode,
                    metadata -> metadata.putAll(
                            parseMap(entity.getUserCodeMetadata())
                    )
            );
        }

        if (entity.getDeviceCodeValue() != null) {
            OAuth2DeviceCode deviceCode =
                    new OAuth2DeviceCode(
                            entity.getDeviceCodeValue(),
                            entity.getDeviceCodeIssuedAt(),
                            entity.getDeviceCodeExpiresAt()
                    );

            builder.token(
                    deviceCode,
                    metadata -> metadata.putAll(
                            parseMap(entity.getDeviceCodeMetadata())
                    )
            );
        }

        return builder.build();
    }

    private void setTokenValues(
            OAuth2Authorization.Token<? extends OAuth2Token> token,
            Consumer<String> tokenValueConsumer,
            Consumer<Instant> issuedAtConsumer,
            Consumer<Instant> expiresAtConsumer,
            Consumer<String> metadataConsumer) {

        if (token == null) {
            return;
        }

        OAuth2Token tokenValue = token.getToken();

        tokenValueConsumer.accept(tokenValue.getTokenValue());
        issuedAtConsumer.accept(tokenValue.getIssuedAt());
        expiresAtConsumer.accept(tokenValue.getExpiresAt());
        metadataConsumer.accept(writeMap(token.getMetadata()));
    }

    private AuthorizationGrantType resolveAuthorizationGrantType(
            String value) {

        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue()
                .equals(value)) {
            return AuthorizationGrantType.AUTHORIZATION_CODE;
        }

        if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()
                .equals(value)) {
            return AuthorizationGrantType.CLIENT_CREDENTIALS;
        }

        if (AuthorizationGrantType.REFRESH_TOKEN.getValue()
                .equals(value)) {
            return AuthorizationGrantType.REFRESH_TOKEN;
        }

        return new AuthorizationGrantType(value);
    }

    private String writeMap(Map<String, ?> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(data);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(
                    "Failed to serialize authorization data.",
                    exception
            );
        }
    }

    private Map<String, Object> parseMap(String data) {
        if (data == null || data.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(
                    data,
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(
                    "Failed to deserialize authorization data.",
                    exception
            );
        }
    }
}
