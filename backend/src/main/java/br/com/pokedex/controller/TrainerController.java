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

import br.com.pokedex.dto.TrainerRequest;
import br.com.pokedex.dto.TrainerResponse;
import br.com.pokedex.service.TrainerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    /** {@code GET /api/trainers} — lista os treinadores ({@code ?pokemonId=25} filtra por Pokémon). */
    @GetMapping
    public List<TrainerResponse> findAll(@RequestParam(required = false) Integer pokemonId) {
        return trainerService.findAll(pokemonId);
    }

    @GetMapping("/{id}")
    public TrainerResponse findById(@PathVariable Integer id) {
        return trainerService.findById(id);
    }

    @PostMapping
    public ResponseEntity<TrainerResponse> create(@Valid @RequestBody TrainerRequest request) {
        TrainerResponse created = trainerService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public TrainerResponse update(@PathVariable Integer id, @Valid @RequestBody TrainerRequest request) {
        return trainerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        trainerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
