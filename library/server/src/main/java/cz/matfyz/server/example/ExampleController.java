package cz.matfyz.server.example;

import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.category.SchemaCategoryController.SchemaCategoryInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ExampleController {

    @Autowired
    @Qualifier("basicExampleSetup")
    cz.matfyz.server.example.basic.ExampleSetup basicExampleSetup;

    @Autowired
    @Qualifier("adminerExampleSetup")
    cz.matfyz.server.example.adminer.ExampleSetup adminerExampleSetup;

    @Autowired
    @Qualifier("queryEvolutionExampleSetup")
    cz.matfyz.server.example.queryevolution.ExampleSetup queryEvolutionExampleSetup;

    @Autowired
    @Qualifier("inferenceExampleSetup")
    cz.matfyz.server.example.inference.ExampleSetup inferenceExampleSetup;

    @PostMapping("/example-schema/{name}")
    public SchemaCategoryInfo createExampleCategory(@PathVariable String name) {
        final SchemaCategoryEntity categoryEntity = switch (name) {
            case "basic" -> basicExampleSetup.setup();
            case "adminer" -> adminerExampleSetup.setup();
            case "query-evolution-1" -> queryEvolutionExampleSetup.setup(1);
            case "query-evolution-2" -> queryEvolutionExampleSetup.setup(2);
            case "query-evolution-3" -> queryEvolutionExampleSetup.setup(3);
            case "query-evolution-4" -> queryEvolutionExampleSetup.setup(4);
            case "inference" -> inferenceExampleSetup.setup();
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };

        return SchemaCategoryInfo.fromEntity(categoryEntity);
    }

}
