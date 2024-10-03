package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.evolution.SchemaUpdate;
import cz.matfyz.server.service.EvolutionService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class EvolutionController {

    @Autowired
    private EvolutionService service;

    @GetMapping("/schema-categories/{id}/updates")
    public List<SchemaUpdate> getCategoryUpdates(@PathVariable Id id) {
        final var updates = service.findAllUpdates(id);

        if (updates == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return updates;
    }

}
