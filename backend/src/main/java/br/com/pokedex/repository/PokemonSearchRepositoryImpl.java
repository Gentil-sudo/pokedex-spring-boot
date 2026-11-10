package br.com.pokedex.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import br.com.pokedex.model.Pokemon;

/**
 * Implementação do fragmento de busca. O sufixo {@code Impl} faz o Spring Data
 * associá-la automaticamente ao {@link PokemonRepository}.
 */
class PokemonSearchRepositoryImpl implements PokemonSearchRepository {

    private static final Pattern NUMBER = Pattern.compile("\\d{1,9}");

    private final MongoTemplate mongoTemplate;

    PokemonSearchRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Pokemon> search(String search, String type) {
        List<Criteria> filters = new ArrayList<>();

        if (StringUtils.hasText(search)) {
            String term = search.trim();
            // Pattern.quote evita que caracteres especiais do usuário virem expressão regular.
            Criteria byName = Criteria.where("name").regex(Pattern.quote(term), "i");
            if (NUMBER.matcher(term).matches()) {
                filters.add(new Criteria().orOperator(byName, Criteria.where("_id").is(Integer.parseInt(term))));
            } else {
                filters.add(byName);
            }
        }

        if (StringUtils.hasText(type)) {
            // "types" é um array: o MongoDB compara cada elemento automaticamente.
            filters.add(Criteria.where("types").regex("^" + Pattern.quote(type.trim()) + "$", "i"));
        }

        Query query = filters.isEmpty()
                ? new Query()
                : new Query(new Criteria().andOperator(filters));
        query.with(Sort.by(Sort.Direction.ASC, "_id"));

        return mongoTemplate.find(query, Pokemon.class);
    }
}
