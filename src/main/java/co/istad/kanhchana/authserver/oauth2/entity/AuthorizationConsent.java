package co.istad.kanhchana.authserver.oauth2.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "oauth2_authorization_consent")
@IdClass(AuthorizationConsent.AuthorizationConsentId.class)
public class AuthorizationConsent {

    @Id
    private String registeredClientId;

    @Id
    private String principalName;

    @Column(length = 1000, nullable = false)
    private String authorities;

    @Getter
    @Setter
    public static class AuthorizationConsentId
            implements Serializable {

        private String registeredClientId;
        private String principalName;

        public AuthorizationConsentId() {
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }

            if (!(object instanceof AuthorizationConsentId other)) {
                return false;
            }

            return Objects.equals(
                    registeredClientId,
                    other.registeredClientId
            ) && Objects.equals(
                    principalName,
                    other.principalName
            );
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    registeredClientId,
                    principalName
            );
        }
    }
}
