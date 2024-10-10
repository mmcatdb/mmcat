package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.instance.InstanceCategoryWrapper;
import cz.matfyz.server.repository.InstanceCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InstanceCategoryController {

    @Autowired
    private InstanceCategoryRepository repository;

    @GetMapping("/instances")
    public InstanceCategoryWrapper getInstanceCategory(@CookieValue("session") Id sessionId) {
        return repository.find(sessionId);
    }

}
