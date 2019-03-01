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

import io.github.ust.mico.core.model.MicoApplication;
import io.github.ust.mico.core.model.MicoService;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * Validates the data of MICO entities like {@link MicoApplication} or {@link MicoService}.
 * It is used in conjunction with standard validation annotations like {@code @NotEmpty} or {@code @Pattern}
 * to be able to validate data only when it is necessary.
 * For example the validation of the {@code name} property of a {@link MicoService} object
 * is only required during its creation or when it is updated, but must not validated if the already existing object
 * should be added to a {@link MicoApplication} object ({@code shortName} and {@code version} should be enough).
 */
@Component
public class MicoDataValidator {

    public void validateMicoApplication(MicoApplication application, Errors errors) {
        if (StringUtils.isEmpty(application.getName())) {
            errors.rejectValue("name", "micoApplication.name.empty", "must not be empty");
        }
        if (application.getDescription() == null) {
            errors.rejectValue("description", "micoApplication.description.isNull", "must not be null");
        }
    }

    public void validateMicoApplication(String shortName, String version, MicoApplication application, Errors errors) {
        if (!application.getShortName().equals(shortName)) {
            errors.rejectValue("shortName", "micoApplication.shortName.inconsistent",
                "does not match request parameter");
        }
        if (!application.getVersion().equals(version)) {
            errors.rejectValue("version", "micoApplication.version.inconsistent",
                "does not match request parameter");
        }
        this.validateMicoApplication(application, errors);
    }

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

    public void validateMicoService(String shortName, String version, MicoService service, Errors errors) {
        if (!service.getShortName().equals(shortName)) {
            errors.rejectValue("shortName", "micoService.shortName.inconsistent",
                "does not match request parameter");
        }
        if (!service.getVersion().equals(version)) {
            errors.rejectValue("version", "micoService.version.inconsistent",
                "does not match request parameter");
        }
        this.validateMicoService(service, errors);
    }
}
