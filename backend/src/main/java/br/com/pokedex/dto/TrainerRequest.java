package br.com.pokedex.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Corpo das requisições POST/PUT de treinadores. */
public record TrainerRequest(
        @Positive(message = "O ID deve ser um número positivo")
        Integer id,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 30, message = "O nome deve ter no máximo 30 caracteres")
        String name,

        @Size(max = 40, message = "O papel deve ter no máximo 40 caracteres")
        String role,

        @NotNull(message = "A quantidade de insígnias é obrigatória")
        @Min(value = 0, message = "As insígnias não podem ser negativas")
        @Max(value = 99, message = "As insígnias devem ser no máximo 99")
        Integer badges,

        @NotNull(message = "Informe a lista de Pokémon (pode ser vazia)")
        @Size(max = 6, message = "Um time pode ter no máximo 6 Pokémon")
        List<@NotNull @Positive Integer> pokemonIds
) {
}
