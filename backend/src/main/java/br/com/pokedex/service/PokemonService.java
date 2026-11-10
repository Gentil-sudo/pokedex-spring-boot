package br.com.pokedex.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import br.com.pokedex.dto.PokemonRequest;
import br.com.pokedex.exception.ConflictException;
import br.com.pokedex.exception.ResourceNotFoundException;
import br.com.pokedex.model.Pokemon;
import br.com.pokedex.repository.PokemonRepository;
import br.com.pokedex.repository.TrainerRepository;

/** Regras de negócio dos Pokémon (CRUD, busca e validações). */
@Service
public class PokemonService {

    private final PokemonRepository pokemonRepository;
    private final TrainerRepository trainerRepository;
    private final PokemonTypeService typeService;

    public PokemonService(PokemonRepository pokemonRepository, TrainerRepository trainerRepository,
                          PokemonTypeService typeService) {
        this.pokemonRepository = pokemonRepository;
        this.trainerRepository = trainerRepository;
        this.typeService = typeService;
    }

    /** Lista todos os Pokémon ou filtra por nome/ID ({@code search}) e/ou tipo. */
    public List<Pokemon> findAll(String search, String type) {
        if (!StringUtils.hasText(search) && !StringUtils.hasText(type)) {
            return pokemonRepository.findAllByOrderByIdAsc();
        }
        return pokemonRepository.search(search, type);
    }

    public Pokemon findById(Integer id) {
        return pokemonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pokémon com ID %d não encontrado".formatted(id)));
    }

    public Pokemon create(PokemonRequest request) {
        Integer id = request.id() != null ? request.id() : nextId();
        if (pokemonRepository.existsById(id)) {
            throw new ConflictException("Já existe um Pokémon com o ID %d".formatted(id));
        }
        ensureNameIsAvailable(request.name(), id);

        Pokemon pokemon = new Pokemon();
        pokemon.setId(id);
        apply(request, pokemon);
        return pokemonRepository.save(pokemon);
    }

    public Pokemon update(Integer id, PokemonRequest request) {
        Pokemon pokemon = findById(id);
        ensureNameIsAvailable(request.name(), id);
        apply(request, pokemon);
        return pokemonRepository.save(pokemon);
    }

    /** Remove o Pokémon e também o retira dos times dos treinadores (integridade referencial). */
    public void delete(Integer id) {
        if (!pokemonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pokémon com ID %d não encontrado".formatted(id));
        }
        pokemonRepository.deleteById(id);
        trainerRepository.removePokemonFromAllTeams(id);
    }

    private void apply(PokemonRequest request, Pokemon pokemon) {
        pokemon.setName(request.name().trim());
        pokemon.setHp(request.hp());
        pokemon.setAttack(request.attack());
        pokemon.setDefense(request.defense());
        pokemon.setSpecialAttack(request.specialAttack());
        pokemon.setSpecialDefense(request.specialDefense());
        pokemon.setSpeed(request.speed());
        pokemon.setTypes(typeService.normalize(request.types()));
        pokemon.setImageUrl(StringUtils.hasText(request.imageUrl()) ? request.imageUrl().trim() : null);
    }

    private void ensureNameIsAvailable(String name, Integer currentId) {
        pokemonRepository.findFirstByNameIgnoreCase(name.trim())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("Já existe um Pokémon chamado '%s' (ID %d)"
                            .formatted(existing.getName(), existing.getId()));
                });
    }

    private Integer nextId() {
        return pokemonRepository.findTopByOrderByIdDesc()
                .map(last -> last.getId() + 1)
                .orElse(1);
    }
}
