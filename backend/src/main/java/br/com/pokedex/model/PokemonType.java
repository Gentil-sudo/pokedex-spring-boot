package br.com.pokedex.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Documento da coleção {@code types} (tabela {@code Types} do script SQL). */
@Document("types")
public class PokemonType {

    @Id
    private Integer id;
    private String name;

    public PokemonType() {
    }

    public PokemonType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
