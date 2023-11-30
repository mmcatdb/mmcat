package cz.matfyz.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author jachym.bartik
 */
@RestController
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "Server is running.";
    }

    @Autowired
    @Qualifier("basicExampleSetup")
    cz.matfyz.server.example.basic.ExampleSetup basicExampleSetup;

    @Autowired
    @Qualifier("queryEvolutionExampleSetup")
    cz.matfyz.server.example.queryevolution.ExampleSetup queryEvolutionExampleSetup;

    @PostMapping("/example-schema/{name}")
    public void addExampleSchema(@PathVariable String name) {
        switch (name) {
            case "basic" -> basicExampleSetup.setup();
            case "query-evolution" -> queryEvolutionExampleSetup.setup();
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

}
