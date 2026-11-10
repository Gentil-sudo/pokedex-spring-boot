package br.com.pokedex.dto;

import java.util.List;

/**
 * Treinador com o time "resolvido": em vez de só os IDs, traz o nome, os tipos e a imagem
 * de cada Pokémon (equivalente a um JOIN entre Trainers, Rel_Trainer_Pokemon e Pokemon).
 */
public record TrainerResponse(
        Integer id,
        String name,
        String role,
        int badges,
        List<Integer> pokemonIds,
        List<TeamMember> pokemon
) {

    public record TeamMember(Integer id, String name, List<String> types, String imageUrl) {
    }
}
