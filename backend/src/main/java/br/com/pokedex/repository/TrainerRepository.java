package br.com.pokedex.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import br.com.pokedex.model.Trainer;

public interface TrainerRepository extends MongoRepository<Trainer, Integer> {

    List<Trainer> findAllByOrderByIdAsc();

    /** Treinadores que possuem o Pokémon informado no time. */
    List<Trainer> findByPokemonIdsOrderByIdAsc(Integer pokemonId);

    /** Remove o Pokémon (todas as ocorrências) do time de todos os treinadores. */
    @Query("{ 'pokemonIds' : ?0 }")
    @Update("{ '$pull' : { 'pokemonIds' : ?0 } }")
    long removePokemonFromAllTeams(Integer pokemonId);

    Optional<Trainer> findTopByOrderByIdDesc();
}
