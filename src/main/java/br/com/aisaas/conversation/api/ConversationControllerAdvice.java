package br.com.aisaas.conversation.api;

import br.com.aisaas.conversation.domain.exceptions.ConversationNotFoundException;
import br.com.aisaas.conversation.domain.exceptions.GeminiAPINotRespondException;
import br.com.aisaas.conversation.domain.exceptions.InvalidMessageException;
import br.com.aisaas.conversation.domain.exceptions.InvalidRouterResponseException;
import br.com.aisaas.conversation.domain.exceptions.MediaTypeNotSuportedException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import br.com.aisaas.shared.api.dto.ErrorResponse;

import java.io.IOException;

@RestControllerAdvice(assignableTypes = ConversationController.class)
public class ConversationControllerAdvice {

    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<ErrorResponse> conversationNotFound(ConversationNotFoundException e){
        return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON).body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(GeminiAPINotRespondException.class)
    public ResponseEntity<ErrorResponse> geminiApiNotRespond(GeminiAPINotRespondException e){
        return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON).body(new ErrorResponse("TECHNICAL_ERROR", e.getMessage()));
    }

    @ExceptionHandler(InvalidMessageException.class)
    public ResponseEntity<ErrorResponse> invalidMessage(InvalidMessageException e){
        return ResponseEntity.status(422).contentType(MediaType.APPLICATION_JSON).body(new ErrorResponse("INVALID", e.getMessage()));
    }

    @ExceptionHandler(InvalidRouterResponseException.class)
    public ResponseEntity<ErrorResponse> invalidRouterResponse(InvalidRouterResponseException e){
        return ResponseEntity.status(409).contentType(MediaType.APPLICATION_JSON).body(new ErrorResponse("NO_AGENT", e.getMessage()));
    }

    @ExceptionHandler(MediaTypeNotSuportedException.class)
    public ResponseEntity<ErrorResponse> mediaTypeNotSupported(MediaTypeNotSuportedException e){
        return ResponseEntity.status(415).contentType(MediaType.APPLICATION_JSON).body(new ErrorResponse("UNSUPPORTED_MEDIA", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> ioException(RuntimeException e){
        return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON).body(new ErrorResponse("RUNTIME_ERROR", e.getMessage()));
    }
}
