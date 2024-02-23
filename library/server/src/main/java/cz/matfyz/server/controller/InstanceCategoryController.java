package cz.matfyz.server.controller;

import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.Key;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.service.InstanceCategoryService;
import cz.matfyz.server.view.InstanceMorphismWrapper;
import cz.matfyz.server.view.InstanceObjectWrapper;

import java.util.List;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author jachym.bartik
 */
@RestController
public class InstanceCategoryController {

    @Autowired
    private InstanceCategoryService service;

    @GetMapping("/instances")
    public List<String> getAllInstances(HttpSession session) {
        var instances = service.findAll(session);

        return instances.stream().map(entry -> entry.getKey() + ":\n" + entry.getValue().toString()).toList();
    }

    @GetMapping("/instances/{categoryId}/objects/{objectKey}")
    public InstanceObjectWrapper getInstanceObject(HttpSession session, @PathVariable Id categoryId, @PathVariable Integer objectKey) {
        final var key = new Key(objectKey);

        final var object = service.findObject(session, categoryId, key);

        if (object == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return new InstanceObjectWrapper(object);
    }

    @GetMapping("/instances/{categoryId}/morphisms/{signatureString}")
    public InstanceMorphismWrapper getInstanceMorphism(HttpSession session, @PathVariable Id categoryId, @PathVariable String signatureString) {
        final var signature = Signature.fromString(signatureString);
        if (signature == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        if (signature.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        final var morphism = service.findMorphism(session, categoryId, signature);

        if (morphism == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return new InstanceMorphismWrapper(morphism);
    }

}
