package io.github.ust.mico.core.model;

/**
 * Enumeration for the various places
 * a service may originate from.
 */
public enum MicoServiceCrawlingOrigin {

    /**
     * Indicates that a service originates
     * from some GitHub repository.
     */
    GITHUB,
    
    /**
     * Indicates that a service originates
     * from Docker.
     */
    DOCKER,
    
    /**
     * Undefined.
     */
    NOT_DEFINED;

}
