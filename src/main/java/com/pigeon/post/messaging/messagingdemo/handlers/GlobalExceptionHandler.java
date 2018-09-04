package com.pigeon.post.messaging.messagingdemo.handlers;

import com.pigeon.post.messaging.messagingdemo.model.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity handleResourceNotFoundException(MethodArgumentNotValidException ex) {
        BindingResult violations = ex.getBindingResult();
        MessageResponse messageResponse = new MessageResponse();
        if (violations.hasErrors()) {
            messageResponse.setError(ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.badRequest().body(messageResponse);
        }
        messageResponse.setError("unknown failure");
        return ResponseEntity.unprocessableEntity().body(messageResponse);
    }
}

