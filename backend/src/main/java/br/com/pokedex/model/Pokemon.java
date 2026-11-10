package br.com.pokedex.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento da coleção {@code pokemons}.
 *
 * <p>Os tipos ficam embutidos no próprio documento (desnormalização), substituindo a
 * tabela de relacionamento {@code Rel_Pokemon_Type} do modelo relacional. Assim, uma única
 * leitura traz o Pokémon completo, sem necessidade de "joins".
 */
@Document("pokemons")
public class Pokemon {

    /** Número da Pokédex, usado como {@code _id} do documento. */
    @Id
    private Integer id;
    private String name;
    private int hp;
    private int attack;
    private int defense;
    private int specialAttack;
    private int specialDefense;
    private int speed;
    private List<String> types = new ArrayList<>();
    /** URL opcional de imagem (útil para Pokémon cadastrados manualmente). */
    private String imageUrl;

    public Pokemon() {
    }

    public Pokemon(Integer id, String name, int hp, int attack, int defense,
                   int specialAttack, int specialDefense, int speed, List<String> types) {
        this.id = id;
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        this.types = new ArrayList<>(types);
    }

    /** Soma de todos os atributos base (exposta no JSON como {@code total}). */
    public int getTotal() {
        return hp + attack + defense + specialAttack + specialDefense + speed;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getSpecialAttack() { return specialAttack; }
    public void setSpecialAttack(int specialAttack) { this.specialAttack = specialAttack; }

    public int getSpecialDefense() { return specialDefense; }
    public void setSpecialDefense(int specialDefense) { this.specialDefense = specialDefense; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public List<String> getTypes() { return types; }
    public void setTypes(List<String> types) { this.types = types; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
