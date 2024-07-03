package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.evolution.SchemaUpdate;
import cz.matfyz.server.entity.evolution.SchemaUpdateInit;
import cz.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.MetadataUpdate;
import cz.matfyz.server.service.SchemaCategoryService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class SchemaCategoryController {

    @Autowired
    private SchemaCategoryService service;

    @GetMapping("/schema-categories")
    public List<SchemaCategoryInfo> getAllCategoryInfos() {
        return service.findAllInfos();
    }

    @PostMapping("/schema-categories")
    public SchemaCategoryInfo createNewCategory(@RequestBody SchemaCategoryInit init) {
        var newInfo = service.createNewInfo(init);
        if (newInfo == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        return newInfo;
    }

    @GetMapping("/schema-categories/{id}/info")
    public SchemaCategoryInfo getCategoryInfo(@PathVariable Id id) {
        SchemaCategoryInfo schema;


        schema = service.findInfo(id);

        if (schema == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return schema;
    }

    @GetMapping("/schema-categories/{id}")
    public SchemaCategoryWrapper getCategoryWrapper(@PathVariable Id id) {
        SchemaCategoryWrapper schema = service.find(id);

        if (schema == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return schema;
    }

    @PostMapping("/schema-categories/{id}/updates")
    public SchemaCategoryWrapper updateCategoryWrapper(@PathVariable Id id, @RequestBody SchemaUpdateInit update) {
        SchemaCategoryWrapper updatedWrapper = service.update(id, update);

        if (updatedWrapper == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        return updatedWrapper;
    }

    @GetMapping("/schema-categories/{id}/updates")
    public List<SchemaUpdate> getCategoryUpdates(@PathVariable Id id) {
        final var updates = service.findAllUpdates(id);

        if (updates == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return updates;
    }

    @PutMapping("/schema-categories/{id}/metadata")
    public void updateCategoryMetadata(@PathVariable Id id, @RequestBody List<MetadataUpdate> metadataUpdates) {
        if (!service.updateMetadata(id, metadataUpdates))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    /*
     * Method for receiving the SchemaCategoryWrapper which came from mminfer
     * Probs should get rid of the return value, and only have void
     * */
    @PostMapping("/schema-categories/store")
    public ResponseEntity<String> storeSchemaCategoryWrapper(@RequestBody SchemaCategoryWrapper wrapper) {
        service.createNewInfo(wrapper);
        return ResponseEntity.ok("Got your request");
    }

}
