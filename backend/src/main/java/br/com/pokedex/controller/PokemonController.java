package br.com.pokedex.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.pokedex.dto.PokemonRequest;
import br.com.pokedex.dto.TrainerResponse;
import br.com.pokedex.model.Pokemon;
import br.com.pokedex.service.PokemonService;
import br.com.pokedex.service.TrainerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pokemons")
public class PokemonController {

    private final PokemonService pokemonService;
    private final TrainerService trainerService;

    public PokemonController(PokemonService pokemonService, TrainerService trainerService) {
        this.pokemonService = pokemonService;
        this.trainerService = trainerService;
    }

    /**
     * {@code GET /api/pokemons} — lista todos os Pokémon.
     * <ul>
     *   <li>{@code ?search=pika} — busca por parte do nome ou pelo ID exato ({@code ?search=25})</li>
     *   <li>{@code ?type=Fire} — filtra por tipo</li>
     * </ul>
     */
    @GetMapping
    public List<Pokemon> findAll(@RequestParam(required = false) String search,
                                 @RequestParam(required = false) String type) {
        return pokemonService.findAll(search, type);
    }

    /** {@code GET /api/pokemons/{id}} — busca um Pokémon pelo ID. */
    @GetMapping("/{id}")
    public Pokemon findById(@PathVariable Integer id) {
        return pokemonService.findById(id);
    }

    /** {@code GET /api/pokemons/{id}/trainers} — treinadores que possuem o Pokémon. */
    @GetMapping("/{id}/trainers")
    public List<TrainerResponse> findTrainers(@PathVariable Integer id) {
        pokemonService.findById(id);
        return trainerService.findAll(id);
    }

    /** {@code POST /api/pokemons} — cadastra um Pokémon (201 + header Location). */
    @PostMapping
    public ResponseEntity<Pokemon> create(@Valid @RequestBody PokemonRequest request) {
        Pokemon created = pokemonService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** {@code PUT /api/pokemons/{id}} — atualiza um Pokémon. */
    @PutMapping("/{id}")
    public Pokemon update(@PathVariable Integer id, @Valid @RequestBody PokemonRequest request) {
        return pokemonService.update(id, request);
    }

    /** {@code DELETE /api/pokemons/{id}} — remove um Pokémon (204). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        pokemonService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
