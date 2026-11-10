package br.com.pokedex.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Converte exceções em respostas JSON padronizadas (RFC 9457 - Problem Details).
 *
 * <pre>
 * { "status": 404, "title": "Não encontrado", "detail": "Pokémon com ID 999 não encontrado" }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail handleConflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    ProblemDetail handleBusiness(BusinessException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Requisição inválida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Valor inválido para '%s': %s".formatted(ex.getName(), ex.getValue()));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    ProblemDetail handleDatabaseDown(DataAccessResourceFailureException ex) {
        log.error("Falha ao acessar o MongoDB", ex);
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Banco de dados indisponível",
                "Não foi possível conectar ao MongoDB. Verifique a variável MONGODB_URI.");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Erro inesperado", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Ocorreu um erro inesperado no servidor.");
    }

    /** Erros de Bean Validation (@Valid): devolve a mensagem de cada campo em "errors". */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos estão inválidos.");
        body.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    /** JSON malformado ou com tipos errados (ex.: "hp": "abc"). */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "JSON inválido",
                "O corpo da requisição não é um JSON válido ou possui campos com tipo incorreto."));
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
