package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.service.InstanceCategoryService;

import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author jachym.bartik
 */
@RestController
public class InstanceCategoryController {

    @Autowired
    private InstanceCategoryService service;

    @GetMapping("/instances")
    public List<String> getAllInstances(HttpSession session) {
        var instances = service.findAll(session);

        return instances.stream().map(instance -> instance.toString()).toList();
    }

}
