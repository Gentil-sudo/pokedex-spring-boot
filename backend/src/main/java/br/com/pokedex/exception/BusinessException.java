package br.com.pokedex.exception;

/** Regra de negócio violada (ex.: tipo inexistente) → HTTP 400. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
