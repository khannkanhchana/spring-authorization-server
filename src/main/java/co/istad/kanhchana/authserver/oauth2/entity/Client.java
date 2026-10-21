package co.istad.kanhchana.authserver.oauth2.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "oauth2_client")
public class Client {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String clientId;

    private Instant clientIdIssuedAt;

    private String clientSecret;

    private Instant clientSecretExpiresAt;

    @Column(nullable = false)
    private String clientName;

    @Column(length = 1000, nullable = false)
    private String clientAuthenticationMethods;

    @Column(length = 1000, nullable = false)
    private String authorizationGrantTypes;

    @Column(length = 1000)
    private String redirectUris;

    @Column(length = 1000)
    private String postLogoutRedirectUris;

    @Column(length = 1000, nullable = false)
    private String scopes;

    @Column(length = 2000, nullable = false)
    private String clientSettings;

    @Column(length = 2000, nullable = false)
    private String tokenSettings;
}
