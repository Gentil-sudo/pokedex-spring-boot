package br.com.pokedex.exception;

/** Conflito com dados existentes (ID ou nome repetido) → HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
