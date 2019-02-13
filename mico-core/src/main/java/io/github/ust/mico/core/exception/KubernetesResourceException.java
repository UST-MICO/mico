package io.github.ust.mico.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Used to indicate that there is a problem concercing
 * a Kubernetes resource, e.g., a Deployment cannot be found
 * or there are multiple results for a query for a resource
 * that is expected to be unique.
 */
@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class KubernetesResourceException extends Exception {

    private static final long serialVersionUID = 6203468717140353398L;

    public KubernetesResourceException() {
        super();
    }
    
    public KubernetesResourceException(String message) {
        super(message);
        
    }

}
