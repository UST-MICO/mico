package io.github.ust.mico.core.web;

import lombok.Getter;


/**
 * This class is used for providing information to the GitHub crawler via the API endpoints.
 */
public class CrawlingInformation {

    /**
     * Required.
     *
     * Link to the GitHub repo to crawl from
     */
    @Getter
    private String uri;

    /**
     * Optional.
     *
     * Can be one of the following:
     * - 'latest'
     * - specific version (e.g., 'v1.0.0')
     */
    @Getter
    private String version = "";
}
