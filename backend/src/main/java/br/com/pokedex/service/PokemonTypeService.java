package br.com.pokedex.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.pokedex.exception.BusinessException;
import br.com.pokedex.model.PokemonType;
import br.com.pokedex.repository.PokemonTypeRepository;

@Service
public class PokemonTypeService {

    private final PokemonTypeRepository typeRepository;

    public PokemonTypeService(PokemonTypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }

    public List<PokemonType> findAll() {
        return typeRepository.findAllByOrderByIdAsc();
    }

    /**
     * Valida os tipos informados contra a coleção {@code types} e devolve os nomes no
     * formato oficial (ex.: "fire" → "Fire"), sem repetições.
     *
     * @throws BusinessException se algum tipo não existir ou houver tipos repetidos
     */
    public List<String> normalize(List<String> types) {
        Map<String, String> official = typeRepository.findAll().stream()
                .map(PokemonType::getName)
                .collect(Collectors.toMap(String::toLowerCase, Function.identity()));

        Set<String> result = new LinkedHashSet<>();
        List<String> unknown = new ArrayList<>();
        for (String type : types) {
            String name = official.get(type.trim().toLowerCase());
            if (name == null) {
                unknown.add(type);
            } else if (!result.add(name)) {
                throw new BusinessException("O tipo '%s' foi informado mais de uma vez".formatted(name));
            }
        }

        if (!unknown.isEmpty()) {
            throw new BusinessException("Tipo(s) inexistente(s): %s. Tipos válidos: %s"
                    .formatted(String.join(", ", unknown), String.join(", ", official.values().stream().sorted().toList())));
        }
        return List.copyOf(result);
    }
}
