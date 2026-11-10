package br.com.pokedex.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import br.com.pokedex.dto.TrainerRequest;
import br.com.pokedex.dto.TrainerResponse;
import br.com.pokedex.dto.TrainerResponse.TeamMember;
import br.com.pokedex.exception.BusinessException;
import br.com.pokedex.exception.ConflictException;
import br.com.pokedex.exception.ResourceNotFoundException;
import br.com.pokedex.model.Pokemon;
import br.com.pokedex.model.Trainer;
import br.com.pokedex.repository.PokemonRepository;
import br.com.pokedex.repository.TrainerRepository;

/** Regras de negócio dos treinadores e da relação treinador ↔ Pokémon. */
@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final PokemonRepository pokemonRepository;

    public TrainerService(TrainerRepository trainerRepository, PokemonRepository pokemonRepository) {
        this.trainerRepository = trainerRepository;
        this.pokemonRepository = pokemonRepository;
    }

    /** Lista todos os treinadores ou apenas os que possuem o Pokémon informado. */
    public List<TrainerResponse> findAll(Integer pokemonId) {
        List<Trainer> trainers = pokemonId == null
                ? trainerRepository.findAllByOrderByIdAsc()
                : trainerRepository.findByPokemonIdsOrderByIdAsc(pokemonId);
        return toResponses(trainers);
    }

    public TrainerResponse findById(Integer id) {
        return toResponses(List.of(getOrThrow(id))).getFirst();
    }

    public TrainerResponse create(TrainerRequest request) {
        Integer id = request.id() != null ? request.id() : nextId();
        if (trainerRepository.existsById(id)) {
            throw new ConflictException("Já existe um treinador com o ID %d".formatted(id));
        }
        Trainer trainer = new Trainer();
        trainer.setId(id);
        apply(request, trainer);
        return findById(trainerRepository.save(trainer).getId());
    }

    public TrainerResponse update(Integer id, TrainerRequest request) {
        Trainer trainer = getOrThrow(id);
        apply(request, trainer);
        return findById(trainerRepository.save(trainer).getId());
    }

    public void delete(Integer id) {
        if (!trainerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Treinador com ID %d não encontrado".formatted(id));
        }
        trainerRepository.deleteById(id);
    }

    private Trainer getOrThrow(Integer id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Treinador com ID %d não encontrado".formatted(id)));
    }

    private void apply(TrainerRequest request, Trainer trainer) {
        ensurePokemonExist(request.pokemonIds());
        trainer.setName(request.name().trim());
        trainer.setRole(StringUtils.hasText(request.role()) ? request.role().trim() : "Treinador");
        trainer.setBadges(request.badges());
        trainer.setPokemonIds(List.copyOf(request.pokemonIds()));
    }

    private void ensurePokemonExist(Collection<Integer> ids) {
        Set<Integer> found = new HashSet<>();
        pokemonRepository.findAllById(new HashSet<>(ids)).forEach(p -> found.add(p.getId()));
        List<Integer> missing = ids.stream().filter(id -> !found.contains(id)).distinct().toList();
        if (!missing.isEmpty()) {
            throw new BusinessException("Pokémon inexistente(s): %s".formatted(missing));
        }
    }

    /** Busca, em uma única consulta, todos os Pokémon referenciados pelos treinadores. */
    private List<TrainerResponse> toResponses(List<Trainer> trainers) {
        Set<Integer> ids = trainers.stream()
                .flatMap(t -> t.getPokemonIds().stream())
                .collect(Collectors.toSet());
        Map<Integer, Pokemon> pokemonById = pokemonRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Pokemon::getId, Function.identity()));

        return trainers.stream()
                .map(trainer -> new TrainerResponse(
                        trainer.getId(),
                        trainer.getName(),
                        trainer.getRole(),
                        trainer.getBadges(),
                        trainer.getPokemonIds(),
                        trainer.getPokemonIds().stream()
                                .map(pokemonById::get)
                                .filter(Objects::nonNull)
                                .map(p -> new TeamMember(p.getId(), p.getName(), p.getTypes(), p.getImageUrl()))
                                .toList()))
                .toList();
    }

    private Integer nextId() {
        return trainerRepository.findTopByOrderByIdDesc()
                .map(last -> last.getId() + 1)
                .orElse(1);
    }
}
