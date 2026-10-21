package co.istad.kanhchana.authserver.oauth2.repository;

import co.istad.kanhchana.authserver.oauth2.entity.Authorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthorizationRepository
        extends JpaRepository<Authorization, String> {

    Optional<Authorization> findByState(String state);

    Optional<Authorization> findByAuthorizationCodeValue(String value);

    Optional<Authorization> findByAccessTokenValue(String value);

    Optional<Authorization> findByRefreshTokenValue(String value);

    Optional<Authorization> findByOidcIdTokenValue(String value);

    Optional<Authorization> findByUserCodeValue(String value);

    Optional<Authorization> findByDeviceCodeValue(String value);

    @Query("""
        SELECT a FROM Authorization a
        WHERE a.state = :token
           OR a.authorizationCodeValue = :token
           OR a.accessTokenValue = :token
           OR a.refreshTokenValue = :token
           OR a.oidcIdTokenValue = :token
           OR a.userCodeValue = :token
           OR a.deviceCodeValue = :token
        """)
    Optional<Authorization> findByToken(
            @Param("token") String token
    );
}