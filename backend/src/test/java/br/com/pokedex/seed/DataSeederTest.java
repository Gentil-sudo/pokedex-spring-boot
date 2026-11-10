package br.com.pokedex.seed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DataSeederTest {

    @Test
    void masksCredentialsOfTheConnectionString() {
        assertThat(DataSeeder.maskCredentials(
                "mongodb+srv://aluno:s3nh4%40forte@cluster0.abc.mongodb.net/?retryWrites=true"))
                .isEqualTo("mongodb+srv://***:***@cluster0.abc.mongodb.net/?retryWrites=true");
    }

    @Test
    void keepsConnectionStringWithoutCredentials() {
        assertThat(DataSeeder.maskCredentials("mongodb://localhost:27017"))
                .isEqualTo("mongodb://localhost:27017");
    }
}
