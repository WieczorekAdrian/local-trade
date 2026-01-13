package io.github.adrian.wieczorek.local_trade.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j // 1. Dodajemy logger
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    problemDetail.setTitle("Authentication Failure");
    problemDetail.setProperty("error_code", "BAD_CREDENTIALS"); // Opcjonalne własne pola
    return problemDetail;
  }

  @ExceptionHandler({AccessDeniedException.class, AccountStatusException.class,
      SignatureException.class, ExpiredJwtException.class})
  public ProblemDetail handleAccessDenied(Exception ex) {
    log.warn("Security exception: {}", ex.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    problemDetail.setTitle("Access Denied");

    if (ex instanceof ExpiredJwtException) {
      problemDetail.setProperty("description", "The JWT token has expired");
    } else if (ex instanceof SignatureException) {
      problemDetail.setProperty("description", "The JWT signature is invalid");
    }

    return problemDetail;
  }

  @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class})
  public ProblemDetail handleNotFound(Exception ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Resource Not Found");
    return problemDetail;
  }

  @ExceptionHandler(GlobalConflictException.class)
  public ProblemDetail handleConflict(GlobalConflictException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problemDetail.setTitle("Conflict");
    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    problemDetail.setTitle("Validation Error");
    problemDetail.setProperty("field_errors", errors);
    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String requiredType =
        (ex.getRequiredType() != null) ? ex.getRequiredType().getSimpleName() : "unknown";
    String detail =
        String.format("Parameter '%s' has an invalid value: '%s'. Required type is '%s'.",
            ex.getName(), ex.getValue(), requiredType);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    problemDetail.setTitle("Type Mismatch");
    return problemDetail;
  }

  @ExceptionHandler(UserLogOutException.class)
  public ProblemDetail handleUserLogOutException(UserLogOutException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    problemDetail.setTitle("Authentication Failure");
    return problemDetail;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGlobalException(Exception ex) {
    log.error("Unexpected error occurred: ", ex);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
        "An internal server error occurred.");
    problemDetail.setTitle("Internal Server Error");
    return problemDetail;
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    problemDetail.setTitle("Method Not Allowed");
    problemDetail.setProperty("timestamp", Instant.now());
    return problemDetail;
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ProblemDetail handleMaxUploadSize(MaxUploadSizeExceededException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE,
        "Przesłane pliki są zbyt duże.");

    problemDetail.setTitle("Przekroczono limit wysyłania");
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("maxFileSize", "10MB");
    problemDetail.setProperty("maxRequestSize", "50MB");

    return problemDetail;
  }
}
