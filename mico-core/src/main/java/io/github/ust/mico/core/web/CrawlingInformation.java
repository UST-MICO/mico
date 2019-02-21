package io.github.ust.mico.core.web;

import lombok.Getter;


public class CrawlingInformation {

    @Getter
    private String uri;

    /**
     * Can be 'latest' or a specific version (e.g. v1.2.3)
     */
    @Getter
    private String version;
}
