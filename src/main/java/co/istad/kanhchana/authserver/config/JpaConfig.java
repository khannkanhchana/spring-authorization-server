
package co.istad.kanhchana.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.server.authorization.jackson.OAuth2AuthorizationServerJacksonModule;

import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JpaConfig {

    @Bean
    public JsonMapper oauth2ObjectMapper() {

        ClassLoader classLoader =
                JpaConfig.class.getClassLoader();

        return JsonMapper.builder()
                .addModules(
                        SecurityJacksonModules.getModules(classLoader)
                )
                .addModule(
                        new OAuth2AuthorizationServerJacksonModule()
                )
                .build();
    }
}
