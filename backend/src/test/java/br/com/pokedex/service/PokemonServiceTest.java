package br.com.pokedex.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.pokedex.dto.PokemonRequest;
import br.com.pokedex.exception.BusinessException;
import br.com.pokedex.exception.ConflictException;
import br.com.pokedex.exception.ResourceNotFoundException;
import br.com.pokedex.model.Pokemon;
import br.com.pokedex.model.PokemonType;
import br.com.pokedex.repository.PokemonRepository;
import br.com.pokedex.repository.PokemonTypeRepository;
import br.com.pokedex.repository.TrainerRepository;

@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock
    private PokemonRepository pokemonRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private PokemonTypeRepository typeRepository;

    private PokemonService service;

    @BeforeEach
    void setUp() {
        service = new PokemonService(pokemonRepository, trainerRepository, new PokemonTypeService(typeRepository));
    }

    private static PokemonRequest request(Integer id, String name, List<String> types) {
        return new PokemonRequest(id, name, 20, 40, 15, 35, 35, 60, types, null);
    }

    private void stubTypes() {
        when(typeRepository.findAll()).thenReturn(List.of(
                new PokemonType(5, "Electric"), new PokemonType(2, "Fire"), new PokemonType(10, "Flying")));
    }

    @Test
    void createUsesNextIdAndNormalizesTypes() {
        stubTypes();
        when(pokemonRepository.findTopByOrderByIdDesc())
                .thenReturn(Optional.of(new Pokemon(151, "Mew", 1, 1, 1, 1, 1, 1, List.of())));
        when(pokemonRepository.existsById(152)).thenReturn(false);
        when(pokemonRepository.findFirstByNameIgnoreCase("Pichu")).thenReturn(Optional.empty());
        when(pokemonRepository.save(any(Pokemon.class))).thenAnswer(inv -> inv.getArgument(0));

        Pokemon created = service.create(request(null, "  Pichu ", List.of("electric")));

        assertThat(created.getId()).isEqualTo(152);
        assertThat(created.getName()).isEqualTo("Pichu");
        assertThat(created.getTypes()).containsExactly("Electric");
        assertThat(created.getTotal()).isEqualTo(205);
    }

    @Test
    void createRejectsDuplicatedName() {
        when(pokemonRepository.existsById(200)).thenReturn(false);
        when(pokemonRepository.findFirstByNameIgnoreCase("pikachu"))
                .thenReturn(Optional.of(new Pokemon(25, "Pikachu", 1, 1, 1, 1, 1, 1, List.of("Electric"))));

        assertThatThrownBy(() -> service.create(request(200, "pikachu", List.of("Electric"))))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Pikachu");
        verify(pokemonRepository, never()).save(any());
    }

    @Test
    void createRejectsUnknownType() {
        stubTypes();
        when(pokemonRepository.existsById(200)).thenReturn(false);
        when(pokemonRepository.findFirstByNameIgnoreCase("Togepi")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request(200, "Togepi", List.of("Fairy"))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Fairy");
    }

    @Test
    void createRejectsRepeatedType() {
        stubTypes();
        when(pokemonRepository.existsById(200)).thenReturn(false);
        when(pokemonRepository.findFirstByNameIgnoreCase("Duplo")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request(200, "Duplo", List.of("Fire", "fire"))))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updateAllowsKeepingTheSameName() {
        stubTypes();
        Pokemon pikachu = new Pokemon(25, "Pikachu", 35, 55, 40, 50, 50, 90, List.of("Electric"));
        when(pokemonRepository.findById(25)).thenReturn(Optional.of(pikachu));
        when(pokemonRepository.findFirstByNameIgnoreCase("Pikachu")).thenReturn(Optional.of(pikachu));
        when(pokemonRepository.save(any(Pokemon.class))).thenAnswer(inv -> inv.getArgument(0));

        Pokemon updated = service.update(25, request(null, "Pikachu", List.of("Electric", "Flying")));

        assertThat(updated.getTypes()).containsExactly("Electric", "Flying");
        assertThat(updated.getHp()).isEqualTo(20);
    }

    @Test
    void deleteRemovesPokemonFromTrainerTeams() {
        when(pokemonRepository.existsById(48)).thenReturn(true);

        service.delete(48);

        verify(pokemonRepository).deleteById(48);
        verify(trainerRepository).removePokemonFromAllTeams(48);
    }

    @Test
    void deleteUnknownPokemonThrowsNotFound() {
        when(pokemonRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(999)).isInstanceOf(ResourceNotFoundException.class);
        verify(trainerRepository, never()).removePokemonFromAllTeams(any());
    }
}
