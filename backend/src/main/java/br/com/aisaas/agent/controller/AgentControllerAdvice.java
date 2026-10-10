package br.com.aisaas.agent.controller;

import br.com.aisaas.agent.domain.exceptions.AgentNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AgentController.class)
public class AgentControllerAdvice {

    @ExceptionHandler(AgentNotFoundException.class)
    public ResponseEntity<AgentNotFoundException> agentNotFoundExceptionResponse(AgentNotFoundException e){
        return ResponseEntity.status(404).body(new AgentNotFoundException("message=" + e.getMessage()));
    }
}
