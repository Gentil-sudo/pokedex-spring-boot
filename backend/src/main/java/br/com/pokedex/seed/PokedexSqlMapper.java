package br.com.pokedex.seed;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.pokedex.model.Pokemon;
import br.com.pokedex.model.PokemonType;
import br.com.pokedex.model.Trainer;

/**
 * Converte as tabelas relacionais do {@code Script_Pokedex.sql} em documentos MongoDB.
 *
 * <pre>
 * Pokemon + Rel_Pokemon_Type + Types   →  pokemons  { _id, name, hp, ..., types: ["Grass", "Poison"] }
 * Types                                 →  types     { _id, name }
 * Trainers + Rel_Trainer_Pokemon        →  trainers  { _id, name, role, badges, pokemonIds: [25, 3, ...] }
 * </pre>
 */
public final class PokedexSqlMapper {

    /**
     * O script não tem o "papel" de cada treinador; completamos com base no anime/jogos
     * para enriquecer a interface. Quem não estiver aqui fica como "Treinador".
     */
    private static final Map<String, String> TRAINER_ROLES = Map.ofEntries(
            Map.entry("Ash", "Treinador"),
            Map.entry("Brock", "Líder de Ginásio"),
            Map.entry("Misty", "Líder de Ginásio"),
            Map.entry("Lt. Surge", "Líder de Ginásio"),
            Map.entry("Erika", "Líder de Ginásio"),
            Map.entry("Koga", "Líder de Ginásio"),
            Map.entry("Sabrina", "Líder de Ginásio"),
            Map.entry("Blaine", "Líder de Ginásio"),
            Map.entry("Giovanni", "Líder de Ginásio"),
            Map.entry("Gary", "Rival"),
            Map.entry("Team Rocket", "Vilões"),
            Map.entry("Lance", "Campeão"));

    public record SeedData(List<PokemonType> types, List<Pokemon> pokemons, List<Trainer> trainers) {
    }

    private PokedexSqlMapper() {
    }

    public static SeedData map(Map<String, SqlScriptParser.Table> tables) {
        // Types: ID_Type → nome
        Map<Integer, String> typeNames = new LinkedHashMap<>();
        for (Map<String, Object> row : rows(tables, "Types")) {
            typeNames.put(intValue(row, "ID_Type"), (String) row.get("Type"));
        }
        List<PokemonType> types = typeNames.entrySet().stream()
                .map(e -> new PokemonType(e.getKey(), e.getValue()))
                .toList();

        // Rel_Pokemon_Type: ID_Pokemon → [nomes dos tipos] (mantém a ordem do script: tipo primário primeiro)
        Map<Integer, List<String>> typesByPokemon = new LinkedHashMap<>();
        for (Map<String, Object> row : rows(tables, "Rel_Pokemon_Type")) {
            String typeName = typeNames.get(intValue(row, "ID_Type"));
            if (typeName == null) {
                throw new IllegalStateException("Tipo inexistente na relação: " + row);
            }
            typesByPokemon.computeIfAbsent(intValue(row, "ID_Pokemon"), id -> new ArrayList<>()).add(typeName);
        }

        List<Pokemon> pokemons = rows(tables, "Pokemon").stream()
                .map(row -> {
                    Integer id = intValue(row, "ID_Pokemon");
                    return new Pokemon(
                            id,
                            (String) row.get("Pokemon"),
                            intValue(row, "HP"),
                            intValue(row, "Attack"),
                            intValue(row, "Defense"),
                            intValue(row, "Special_Attack"),
                            intValue(row, "Special_Defense"),
                            intValue(row, "Speed"),
                            typesByPokemon.getOrDefault(id, List.of()));
                })
                .toList();

        // Rel_Trainer_Pokemon: ID_Trainer → [ID_Pokemon] (mantém repetições, ex.: 3 Venonat do Koga)
        Map<Integer, List<Integer>> teamByTrainer = new LinkedHashMap<>();
        for (Map<String, Object> row : rows(tables, "Rel_Trainer_Pokemon")) {
            teamByTrainer.computeIfAbsent(intValue(row, "ID_Trainer"), id -> new ArrayList<>())
                    .add(intValue(row, "ID_Pokemon"));
        }

        List<Trainer> trainers = rows(tables, "Trainers").stream()
                .map(row -> {
                    Integer id = intValue(row, "ID_Trainer");
                    String name = (String) row.get("Trainer");
                    return new Trainer(
                            id,
                            name,
                            TRAINER_ROLES.getOrDefault(name, "Treinador"),
                            intValue(row, "Badges"),
                            teamByTrainer.getOrDefault(id, List.of()));
                })
                .toList();

        return new SeedData(types, pokemons, trainers);
    }

    private static List<Map<String, Object>> rows(Map<String, SqlScriptParser.Table> tables, String name) {
        SqlScriptParser.Table table = tables.get(name);
        if (table == null) {
            throw new IllegalStateException("Tabela '%s' não encontrada no script SQL".formatted(name));
        }
        return table.rowsAsMaps();
    }

    private static int intValue(Map<String, Object> row, String column) {
        Object value = row.get(column);
        if (!(value instanceof Number number)) {
            throw new IllegalStateException("Coluna %s inválida em %s".formatted(column, row));
        }
        return number.intValue();
    }
}
