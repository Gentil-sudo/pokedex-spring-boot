package br.com.pokedex.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.pokedex.dto.PokemonRequest;
import br.com.pokedex.exception.ResourceNotFoundException;
import br.com.pokedex.model.Pokemon;
import br.com.pokedex.service.PokemonService;
import br.com.pokedex.service.TrainerService;

@WebMvcTest(PokemonController.class)
class PokemonControllerTest {

    private static final String VALID_BODY = """
            {"name":"Pichu","hp":20,"attack":40,"defense":15,
             "specialAttack":35,"specialDefense":35,"speed":60,"types":["Electric"]}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PokemonService pokemonService;
    @MockitoBean
    private TrainerService trainerService;

    private static Pokemon pikachu() {
        return new Pokemon(25, "Pikachu", 35, 55, 30, 50, 40, 90, List.of("Electric"));
    }

    @Test
    void listsPokemonWithFilters() throws Exception {
        when(pokemonService.findAll("pika", "Electric")).thenReturn(List.of(pikachu()));

        mvc.perform(get("/api/pokemons").param("search", "pika").param("type", "Electric"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Pikachu"))
                .andExpect(jsonPath("$[0].types[0]").value("Electric"))
                .andExpect(jsonPath("$[0].total").value(300));
    }

    @Test
    void returns404WhenPokemonDoesNotExist() throws Exception {
        when(pokemonService.findById(999)).thenThrow(new ResourceNotFoundException("Pokémon com ID 999 não encontrado"));

        mvc.perform(get("/api/pokemons/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Pokémon com ID 999 não encontrado"));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        Pokemon pichu = new Pokemon(152, "Pichu", 20, 40, 15, 35, 35, 60, List.of("Electric"));
        when(pokemonService.create(any(PokemonRequest.class))).thenReturn(pichu);

        mvc.perform(post("/api/pokemons").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/pokemons/152"))
                .andExpect(jsonPath("$.id").value(152));
    }

    @Test
    void createValidatesTheBody() throws Exception {
        mvc.perform(post("/api/pokemons").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","hp":0,"attack":300,"types":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.hp").exists())
                .andExpect(jsonPath("$.errors.attack").exists())
                .andExpect(jsonPath("$.errors.defense").exists())
                .andExpect(jsonPath("$.errors.types").exists());
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mvc.perform(post("/api/pokemons").contentType(MediaType.APPLICATION_JSON).content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("JSON inválido"));
    }

    @Test
    void updateDelegatesToService() throws Exception {
        when(pokemonService.update(eq(25), any(PokemonRequest.class))).thenReturn(pikachu());

        mvc.perform(put("/api/pokemons/25").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pikachu"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/pokemons/25")).andExpect(status().isNoContent());
        verify(pokemonService).delete(25);
    }

    @Test
    void deleteUnknownReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Pokémon com ID 999 não encontrado")).when(pokemonService).delete(999);

        mvc.perform(delete("/api/pokemons/999")).andExpect(status().isNotFound());
    }
}
