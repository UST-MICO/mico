/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core.validation;

import io.github.ust.mico.core.dto.MicoApplicationDTO;
import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * Validates the data of MICO entities like {@link MicoApplicationDTO} or {@link MicoService}.
 * It is used in conjunction with standard validation annotations like {@code @NotEmpty} or {@code @Pattern}
 * to be able to validate data only when it is necessary.
 * For example the validation of the {@code name} property of a {@link MicoService} object
 * is only required during its creation or when it is updated, but must not validated if the already existing object
 * should be added to a {@link MicoApplicationDTO} object ({@code shortName} and {@code version} should be enough).
 */
@Component
public class MicoDataValidator {

    /**
     * Validates the name and the description of a {@link MicoApplicationDTO}
     *  @param applicationDto the {@link MicoApplicationDTO}
     * @param errors      the {@link Errors}
     */
    public void validateMicoApplication(MicoApplicationDTO applicationDto, Errors errors) {
        if (StringUtils.isEmpty(applicationDto.getName())) {
            errors.rejectValue("name", "micoApplication.name.empty", "must not be empty");
        }
        if (applicationDto.getDescription() == null) {
            errors.rejectValue("description", "micoApplication.description.isNull", "must not be null");
        }
    }

    /**
     * Validates a {@link MicoApplicationDTO} and also if the required properties short name and version match the provided ones.
     *
     * @param shortName   the short name of a {@link MicoApplicationDTO}
     * @param version     the version of a {@link MicoApplicationDTO}
     * @param applicationDto the {@link MicoApplicationDTO}
     * @param errors      the {@link Errors}
     */
    public void validateMicoApplication(String shortName, String version, MicoApplicationDTO applicationDto, Errors errors) {
        if (!applicationDto.getShortName().equals(shortName)) {
            errors.rejectValue("shortName", "micoApplication.shortName.inconsistent",
                "does not match request parameter");
        }
        if (!applicationDto.getVersion().equals(version)) {
            errors.rejectValue("version", "micoApplication.version.inconsistent",
                "does not match request parameter");
        }
        this.validateMicoApplication(applicationDto, errors);
    }

    /**
     * Validates the name, the description and the Git clone url of a {@link MicoService}
     *
     * @param micoService the {@link MicoService}
     * @param errors      the {@link Errors}
     */
    public void validateMicoService(MicoService micoService, Errors errors) {
        if (StringUtils.isEmpty(micoService.getName())) {
            errors.rejectValue("name", "micoService.name.empty", "must not be empty");
        }
        if (micoService.getDescription() == null) {
            errors.rejectValue("description", "micoService.description.isNull", "must not be null");
        }
        // If the Git clone URL is not empty, it must be a valid GitHub URL
        if (!StringUtils.isEmpty(micoService.getGitCloneUrl())) {
            String[] schemes = {"http", "https"};
            UrlValidator urlValidator = new UrlValidator(schemes);
            if (!urlValidator.isValid(micoService.getGitCloneUrl())) {
                errors.rejectValue("gitCloneUrl", "micoService.gitCloneUrl.invalid", "must be valid URL");
            }
        }
    }

    /**
     * Validates a {@link MicoService} and also if the required properties short name and version match the provided ones.
     *
     * @param shortName   the short name of a {@link MicoService}
     * @param version     the version of a {@link MicoService}
     * @param micoService the {@link MicoService}
     * @param errors      the {@link Errors}
     */
    public void validateMicoService(String shortName, String version, MicoService micoService, Errors errors) {
        if (!micoService.getShortName().equals(shortName)) {
            errors.rejectValue("shortName", "micoService.shortName.inconsistent",
                "does not match request parameter");
        }
        if (!micoService.getVersion().equals(version)) {
            errors.rejectValue("version", "micoService.version.inconsistent",
                "does not match request parameter");
        }
        this.validateMicoService(micoService, errors);
    }
}
