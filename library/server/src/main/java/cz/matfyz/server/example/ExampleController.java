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

    @Autowired
    @Qualifier("tpchExampleSetup")
    cz.matfyz.server.example.tpch.ExampleSetup tpchExampleSetup;

    @Autowired
    @Qualifier("adaptationExampleSetup")
    cz.matfyz.server.example.adaptation.ExampleSetup adaptationExampleSetup;

    @PostMapping("/example-schema/{name}")
    public SchemaCategoryInfo createExampleCategory(@PathVariable String name) {
        final SchemaCategoryEntity categoryEntity = switch (name) {
            case Example.basic -> basicExampleSetup.setup();
            case Example.adminer -> adminerExampleSetup.setup();
            case Example.queryEvolution + ":1" -> queryEvolutionExampleSetup.setup(1);
            case Example.queryEvolution + ":2" -> queryEvolutionExampleSetup.setup(2);
            case Example.queryEvolution + ":3" -> queryEvolutionExampleSetup.setup(3);
            case Example.queryEvolution + ":4" -> queryEvolutionExampleSetup.setup(4);
            case Example.inference -> inferenceExampleSetup.setup();
            case Example.tpch -> tpchExampleSetup.setup();
            case Example.adaptation -> adaptationExampleSetup.setup();
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };

        return SchemaCategoryInfo.fromEntity(categoryEntity);
    }

    public interface Example {
        public final String basic = "basic";
        public final String adminer = "adminer";
        public final String queryEvolution = "query-evolution";
        public final String inference = "inference";
        public final String tpch = "tpch";
        public final String adaptation = "adaptation";
    }

}
