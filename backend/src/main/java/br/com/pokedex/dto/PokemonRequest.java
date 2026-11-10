package br.com.pokedex.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Corpo das requisições POST/PUT de Pokémon.
 *
 * <p>O {@code id} é opcional no cadastro: se não for informado, a API usa o próximo número
 * livre. No PUT ele é ignorado (vale o ID da URL).
 */
public record PokemonRequest(
        @Positive(message = "O ID deve ser um número positivo")
        Integer id,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 30, message = "O nome deve ter no máximo 30 caracteres")
        String name,

        @NotNull(message = "O HP é obrigatório")
        @Min(value = 1, message = "O HP deve ser no mínimo 1")
        @Max(value = 255, message = "O HP deve ser no máximo 255")
        Integer hp,

        @NotNull(message = "O ataque é obrigatório")
        @Min(value = 1, message = "O ataque deve ser no mínimo 1")
        @Max(value = 255, message = "O ataque deve ser no máximo 255")
        Integer attack,

        @NotNull(message = "A defesa é obrigatória")
        @Min(value = 1, message = "A defesa deve ser no mínimo 1")
        @Max(value = 255, message = "A defesa deve ser no máximo 255")
        Integer defense,

        @NotNull(message = "O ataque especial é obrigatório")
        @Min(value = 1, message = "O ataque especial deve ser no mínimo 1")
        @Max(value = 255, message = "O ataque especial deve ser no máximo 255")
        Integer specialAttack,

        @NotNull(message = "A defesa especial é obrigatória")
        @Min(value = 1, message = "A defesa especial deve ser no mínimo 1")
        @Max(value = 255, message = "A defesa especial deve ser no máximo 255")
        Integer specialDefense,

        @NotNull(message = "A velocidade é obrigatória")
        @Min(value = 1, message = "A velocidade deve ser no mínimo 1")
        @Max(value = 255, message = "A velocidade deve ser no máximo 255")
        Integer speed,

        @NotNull(message = "Informe ao menos um tipo")
        @Size(min = 1, max = 2, message = "Informe um ou dois tipos")
        List<@NotBlank(message = "O tipo não pode ser vazio") String> types,

        @Size(max = 500, message = "A URL da imagem deve ter no máximo 500 caracteres")
        @Pattern(regexp = "^(https?://\\S+)?$", message = "A URL da imagem deve começar com http:// ou https://")
        String imageUrl
) {
}
