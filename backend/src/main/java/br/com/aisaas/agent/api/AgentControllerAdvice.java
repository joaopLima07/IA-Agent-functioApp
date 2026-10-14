package br.com.aisaas.agent.api;

import br.com.aisaas.agent.domain.exceptions.AgentNotFoundException;
import br.com.aisaas.agent.domain.exceptions.InvalidAgentConfigException;
import br.com.aisaas.shared.api.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AgentController.class)
public class AgentControllerAdvice {

    @ExceptionHandler(AgentNotFoundException.class)
    public ResponseEntity<ErrorResponse> agentNotFoundExceptionResponse(AgentNotFoundException e){
        return ResponseEntity.status(404).body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(InvalidAgentConfigException.class)
    public ResponseEntity<ErrorResponse> agentConfigExceptionResponse(InvalidAgentConfigException e){
        if (e.getMessage().equals(InvalidAgentConfigException.TEMPERATURE_TOO_HIGH)){
            return ResponseEntity.status(422).body(new ErrorResponse("INVALID_CONFIG", e.getMessage()));
        }

        return ResponseEntity.status(409).body(new ErrorResponse("INVALID_CONFIG", e.getMessage()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> optimisticLockResponse(ObjectOptimisticLockingFailureException e){
        return ResponseEntity.status(409).body(new ErrorResponse("CONFLICT", "Este agente foi modificado por outra pessoa. Recarregue a página e tente novamente."));
    }
}
