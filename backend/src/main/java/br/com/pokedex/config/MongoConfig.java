package br.com.pokedex.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ajustes do cliente MongoDB.
 *
 * <p>O padrão do driver é esperar 30 s por um servidor disponível. Reduzimos para 10 s para
 * que erros de configuração (URI errada, IP não liberado no Atlas...) apareçam rapidamente.
 */
@Configuration
public class MongoConfig {

    @Bean
    MongoClientSettingsBuilderCustomizer fasterServerSelection() {
        return settings -> settings.applyToClusterSettings(
                cluster -> cluster.serverSelectionTimeout(10, TimeUnit.SECONDS));
    }
}
