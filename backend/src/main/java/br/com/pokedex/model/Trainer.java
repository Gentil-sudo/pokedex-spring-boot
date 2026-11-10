package br.com.pokedex.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento da coleção {@code trainers}.
 *
 * <p>A relação N:N {@code Rel_Trainer_Pokemon} vira uma lista de referências
 * ({@code pokemonIds}) dentro do treinador. Usamos referência (e não cópia do Pokémon)
 * porque o mesmo Pokémon pode estar em vários times e ser editado/excluído pela API.
 * A lista mantém a ordem e permite repetições (ex.: Koga possui três Venonat).
 */
@Document("trainers")
public class Trainer {

    @Id
    private Integer id;
    private String name;
    private String role;
    private int badges;
    private List<Integer> pokemonIds = new ArrayList<>();

    public Trainer() {
    }

    public Trainer(Integer id, String name, String role, int badges, List<Integer> pokemonIds) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.badges = badges;
        this.pokemonIds = new ArrayList<>(pokemonIds);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getBadges() { return badges; }
    public void setBadges(int badges) { this.badges = badges; }

    public List<Integer> getPokemonIds() { return pokemonIds; }
    public void setPokemonIds(List<Integer> pokemonIds) { this.pokemonIds = pokemonIds; }
}
