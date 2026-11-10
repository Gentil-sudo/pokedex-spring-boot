package br.com.pokedex.seed;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import br.com.pokedex.repository.PokemonRepository;
import br.com.pokedex.repository.PokemonTypeRepository;
import br.com.pokedex.repository.TrainerRepository;

/**
 * Popula o MongoDB com os dados do {@code Script_Pokedex.sql} ao iniciar a aplicação.
 *
 * <p>Cada coleção só é carregada se estiver vazia, então as alterações feitas pela API
 * (cadastros, edições e exclusões) são preservadas entre reinicializações. Para voltar
 * aos dados originais, inicie com {@code SEED_RESET=true}.
 */
@Component
@ConditionalOnBooleanProperty(name = "app.seed.enabled", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final PokemonRepository pokemonRepository;
    private final PokemonTypeRepository typeRepository;
    private final TrainerRepository trainerRepository;
    private final Resource script;
    private final boolean reset;

    public DataSeeder(PokemonRepository pokemonRepository,
                      PokemonTypeRepository typeRepository,
                      TrainerRepository trainerRepository,
                      @Value("${app.seed.script}") Resource script,
                      @Value("${app.seed.reset:false}") boolean reset) {
        this.pokemonRepository = pokemonRepository;
        this.typeRepository = typeRepository;
        this.trainerRepository = trainerRepository;
        this.script = script;
        this.reset = reset;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (reset) {
            log.warn("SEED_RESET=true: apagando as coleções pokemons, types e trainers");
            pokemonRepository.deleteAll();
            typeRepository.deleteAll();
            trainerRepository.deleteAll();
        }

        boolean needsTypes = typeRepository.count() == 0;
        boolean needsPokemons = pokemonRepository.count() == 0;
        boolean needsTrainers = trainerRepository.count() == 0;
        if (!needsTypes && !needsPokemons && !needsTrainers) {
            log.info("MongoDB já populado ({} Pokémon, {} tipos, {} treinadores)",
                    pokemonRepository.count(), typeRepository.count(), trainerRepository.count());
            return;
        }

        String sql = script.getContentAsString(StandardCharsets.UTF_8);
        PokedexSqlMapper.SeedData data = PokedexSqlMapper.map(SqlScriptParser.parse(sql));

        if (needsTypes) {
            typeRepository.saveAll(data.types());
            log.info("Coleção 'types' populada com {} documentos", data.types().size());
        }
        if (needsPokemons) {
            pokemonRepository.saveAll(data.pokemons());
            log.info("Coleção 'pokemons' populada com {} documentos", data.pokemons().size());
        }
        if (needsTrainers) {
            trainerRepository.saveAll(data.trainers());
            log.info("Coleção 'trainers' populada com {} documentos", data.trainers().size());
        }
    }
}
