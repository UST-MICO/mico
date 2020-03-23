package io.github.ust.mico.core.resource;

import java.awt.PageAttributes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value="/patterns", produces = MediaTypes.HAL_JSON_VALUE)
public class PatternResource {
}
