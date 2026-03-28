package ru.goidaai.test_backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        ProblemDetail problemDetail = buildProblem(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            request.getRequestURI()
        );

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException exception, HttpServletRequest request) {
        return buildProblem(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException exception, HttpServletRequest request) {
        return buildProblem(HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleUploadTooLarge(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        return buildProblem(HttpStatus.PAYLOAD_TOO_LARGE, "Uploaded file is too large", request.getRequestURI());
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleErrorResponse(ErrorResponseException exception, HttpServletRequest request) {
        HttpStatusCode statusCode = exception.getStatusCode();
        return buildProblem(
            HttpStatus.valueOf(statusCode.value()),
            exception.getBody().getDetail(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request.getRequestURI());
    }

    private ProblemDetail buildProblem(HttpStatus status, String detail, String path) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setType(URI.create("https://goida.ai/problems/" + status.value()));
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("path", path);
        return problemDetail;
    }
}
