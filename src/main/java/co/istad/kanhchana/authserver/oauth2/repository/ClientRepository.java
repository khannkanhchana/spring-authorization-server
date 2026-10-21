package co.istad.kanhchana.authserver.oauth2.repository;

import co.istad.kanhchana.authserver.oauth2.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository
        extends JpaRepository<Client, String> {

    Optional<Client> findByClientId(String clientId);
}
