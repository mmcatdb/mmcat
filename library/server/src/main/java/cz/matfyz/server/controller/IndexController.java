package cz.matfyz.server.controller;

import cz.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    public SchemaCategoryInfo createExampleCategory(@PathVariable String name) {
        final SchemaCategoryWrapper wrapper = switch (name) {
            case "basic" -> basicExampleSetup.setup();
            case "query-evolution-1" -> queryEvolutionExampleSetup.setup(1);
            case "query-evolution-2" -> queryEvolutionExampleSetup.setup(2);
            case "query-evolution-3" -> queryEvolutionExampleSetup.setup(3);
            case "query-evolution-4" -> queryEvolutionExampleSetup.setup(4);
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };

        return SchemaCategoryInfo.fromWrapper(wrapper);
    }

}
