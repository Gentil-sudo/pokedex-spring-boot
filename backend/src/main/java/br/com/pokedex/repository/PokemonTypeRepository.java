package br.com.pokedex.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.pokedex.model.PokemonType;

public interface PokemonTypeRepository extends MongoRepository<PokemonType, Integer> {

    List<PokemonType> findAllByOrderByIdAsc();
}
