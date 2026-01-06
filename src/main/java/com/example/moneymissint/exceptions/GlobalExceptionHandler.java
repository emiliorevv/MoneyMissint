package com.example.moneymissint.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler  {
    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<Map<String,String>> handleValidationErrors(MethodArgumentNotValidException ex){

        Map <String,String> errormap = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error-> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errormap.put(fieldName, message);
        });
        return ResponseEntity.badRequest().body(errormap);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String>handleIllegalArguments(IllegalArgumentException ex){

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String>handleIllegalStates(IllegalStateException ex){

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String>handleNotFoundEntity(EntityNotFoundException ex){

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String>handleDataIntegrityViolation(DataIntegrityViolationException ex){

        return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Database conflict or duplicate entry detected");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String>handleAllExceptions(Exception e){

        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong, please try again later");
    }
}
