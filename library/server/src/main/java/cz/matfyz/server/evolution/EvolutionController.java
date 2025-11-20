package cz.matfyz.server.evolution;

import cz.matfyz.server.utils.entity.Id;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EvolutionController {

    @Autowired
    private EvolutionRepository repository;

    @GetMapping("/schema-categories/{id}/updates")
    public List<SchemaEvolution> getCategoryUpdates(@PathVariable Id id) {
        return repository.findAllSchemaEvolutions(id);
    }

}
