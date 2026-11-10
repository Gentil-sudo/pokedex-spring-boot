package br.com.pokedex.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import br.com.pokedex.model.Pokemon;
import br.com.pokedex.model.Trainer;

class SqlScriptParserTest {

    @Test
    void parsesStringsWithEscapedQuotesAndNulls() {
        String sql = """
                CREATE TABLE T (`a` int, `b` varchar(20));
                INSERT INTO T
                    (`a`, `b`)
                VALUES
                    (1, 'Farfetch''d'),
                    (2, NULL),
                    (3, 'Mr. Mime, o (mímico)')
                ;
                SELECT * FROM T;
                """;

        SqlScriptParser.Table table = SqlScriptParser.parse(sql).get("T");

        assertThat(table.columns()).containsExactly("a", "b");
        assertThat(table.rows()).hasSize(3);
        assertThat(table.rows().get(0)).containsExactly(1, "Farfetch'd");
        assertThat(table.rows().get(1)).containsExactly(2, null);
        assertThat(table.rows().get(2)).containsExactly(3, "Mr. Mime, o (mímico)");
    }

    @Test
    void mapsTheOfficialScriptToDocuments() throws IOException {
        String sql = new ClassPathResource("data/Script_Pokedex.sql").getContentAsString(StandardCharsets.UTF_8);

        PokedexSqlMapper.SeedData data = PokedexSqlMapper.map(SqlScriptParser.parse(sql));

        assertThat(data.types()).hasSize(18);
        assertThat(data.pokemons()).hasSize(151);
        assertThat(data.trainers()).hasSize(12);

        Map<Integer, Pokemon> byId = data.pokemons().stream()
                .collect(java.util.stream.Collectors.toMap(Pokemon::getId, p -> p));
        Pokemon bulbasaur = byId.get(1);
        assertThat(bulbasaur.getName()).isEqualTo("Bulbasaur");
        assertThat(bulbasaur.getTypes()).containsExactly("Grass", "Poison");
        assertThat(bulbasaur.getSpecialAttack()).isEqualTo(65);

        assertThat(byId.get(25).getTypes()).containsExactly("Electric");
        assertThat(byId.get(83).getName()).isEqualTo("Farfetch'd");
        // Mew não possui linha em Rel_Pokemon_Type no script original
        assertThat(byId.get(151).getTypes()).isEmpty();

        Trainer koga = data.trainers().stream().filter(t -> t.getName().equals("Koga")).findFirst().orElseThrow();
        assertThat(koga.getPokemonIds()).isEqualTo(List.of(48, 48, 48, 49));
        assertThat(koga.getBadges()).isEqualTo(5);
        assertThat(koga.getRole()).isEqualTo("Líder de Ginásio");
    }
}
