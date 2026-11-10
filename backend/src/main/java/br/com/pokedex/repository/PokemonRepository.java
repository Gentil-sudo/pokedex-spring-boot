package br.com.pokedex.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.pokedex.model.Pokemon;

/**
 * Repositório da coleção {@code pokemons}.
 *
 * <p>Os métodos abaixo são "derived queries": o Spring Data gera a consulta MongoDB a
 * partir do nome do método. A busca combinada (nome/ID + tipo) fica em
 * {@link PokemonSearchRepository}.
 */
public interface PokemonRepository extends MongoRepository<Pokemon, Integer>, PokemonSearchRepository {

    List<Pokemon> findAllByOrderByIdAsc();

    Optional<Pokemon> findFirstByNameIgnoreCase(String name);

    Optional<Pokemon> findTopByOrderByIdDesc();
}
