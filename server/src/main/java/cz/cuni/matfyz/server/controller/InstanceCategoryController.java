package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.server.service.InstanceCategoryService;
import cz.cuni.matfyz.server.view.InstanceObjectView;

import java.util.List;
import javax.servlet.http.HttpSession;

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
    public InstanceObjectView getInstanceObject(HttpSession session, @PathVariable Integer categoryId, @PathVariable Integer objectKey) {
        var key = new Key(objectKey);

        var object = service.findObject(session, categoryId, key);

        if (object == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        
        //return object.toJSON().toString();
        return new InstanceObjectView(object);
    }

}
