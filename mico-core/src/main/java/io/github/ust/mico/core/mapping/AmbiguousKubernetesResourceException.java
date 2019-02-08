package io.github.ust.mico.core.mapping;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Used to indicate that a query for some Kubernetes resource(s)
 * is ambiguous, e.g., if looking for a single deployment and
 * the result is a list of mroe than one deployment.
 */
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class AmbiguousKubernetesResourceException extends Exception {

    private static final long serialVersionUID = -8653163931993175651L;
    
    public AmbiguousKubernetesResourceException() {
        super();
    }
    
    public AmbiguousKubernetesResourceException(String message) {
        super(message);
        
    }

}
