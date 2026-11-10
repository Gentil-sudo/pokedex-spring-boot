package br.com.pokedex.repository;

import java.util.List;

import br.com.pokedex.model.Pokemon;

/** Fragmento customizado do {@link PokemonRepository} para buscas com filtros opcionais. */
public interface PokemonSearchRepository {

    /**
     * Busca Pokémon combinando filtros opcionais, ordenando pelo ID.
     *
     * @param search parte do nome (sem diferenciar maiúsculas) ou o ID exato, se for numérico
     * @param type   nome do tipo (sem diferenciar maiúsculas)
     */
    List<Pokemon> search(String search, String type);
}
