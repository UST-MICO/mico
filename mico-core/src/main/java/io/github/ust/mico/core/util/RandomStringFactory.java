package io.github.ust.mico.core.util;

import org.apache.commons.lang3.RandomStringUtils;

import lombok.experimental.UtilityClass;

/**
 * Provides functionality to generate random {@code String}.
 */
@UtilityClass
public class RandomStringFactory {
    
    private final int LENGTH = 8;
    private final boolean LOWERCASE = true;
    
    public final String randomAlphanumeric() {
        if (LOWERCASE) {
            return RandomStringUtils.randomAlphanumeric(LENGTH).toLowerCase(); 
        } else {
            return RandomStringUtils.randomAlphanumeric(LENGTH);
        }
    }

}
