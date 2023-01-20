package cz.cuni.matfyz.server.controller.publicapi;

import cz.cuni.matfyz.server.controller.SchemaCategoryController;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryInit;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jachym.bartik
 */
@CrossOrigin(origins = "*", allowCredentials = "false")
@RestController
public class SchemaCategoryPublicController {

    @Autowired
    private SchemaCategoryController controller;

    @GetMapping("/public/schema-categories")
    public List<SchemaCategoryInfo> getAllCategoryInfos() {
        return controller.getAllCategoryInfos();
    }

    @PostMapping("/public/schema-categories")
    public SchemaCategoryInfo createNewSchema(@RequestBody SchemaCategoryInit init) {
        return controller.createNewSchema(init);
    }

    @GetMapping("/public/schema-categories/{id}/info")
    public SchemaCategoryInfo getCategoryInfo(@PathVariable Id id) {
        return controller.getCategoryInfo(id);
    }

}
