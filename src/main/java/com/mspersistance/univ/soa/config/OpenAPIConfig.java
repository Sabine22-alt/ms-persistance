package com.mspersistance.univ.soa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8090}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort);
        localServer.setDescription("Serveur local de dÃ©veloppement");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("SmartDish - API de Persistance")
                .version("1.0.0")
                .description("API REST pour la gestion de la persistance des donnÃ©es du projet SmartDish. " +
                        "Cette API centralise l'accÃ¨s aux donnÃ©es pour tous les microservices (Utilisateurs, Recettes, Feedbacks, Recommendations).")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
