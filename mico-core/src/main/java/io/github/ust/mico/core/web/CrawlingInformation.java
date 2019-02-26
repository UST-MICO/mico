package io.github.ust.mico.core.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * This class is used for providing information to the GitHub crawler via the API endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrawlingInformation {

    /**
     * Link to the GitHub repo to crawl from (required).
     */
    private String uri;

    /**
     * The GitHub release tag. Defaults to 'latest'.
     */
    private String version = "latest";
}
