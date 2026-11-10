package br.com.pokedex.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.pokedex.model.PokemonType;
import br.com.pokedex.service.PokemonTypeService;

@RestController
@RequestMapping("/api/types")
public class PokemonTypeController {

    private final PokemonTypeService typeService;

    public PokemonTypeController(PokemonTypeService typeService) {
        this.typeService = typeService;
    }

    /** {@code GET /api/types} — tipos cadastrados (usados no filtro e no formulário). */
    @GetMapping
    public List<PokemonType> findAll() {
        return typeService.findAll();
    }
}
