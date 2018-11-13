package io.github.ust.mico.core;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * An example rest controller to test and show springfox.
 */
@RestController
public class ExampleController {

    @GetMapping("/example")
    public String example() {
        //TODO Remove ME after
        return "Example";
    }

}
