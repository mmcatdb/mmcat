package cz.matfyz.server.controller;

import cz.matfyz.server.entity.InstanceCategoryWrapper;
import cz.matfyz.server.global.RequestContext;
import cz.matfyz.server.repository.InstanceCategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InstanceCategoryController {

    @Autowired
    private RequestContext request;

    @Autowired
    private InstanceCategoryRepository repository;

    @GetMapping("/instances")
    public InstanceCategoryWrapper getInstanceCategory() {
        return repository.find(request.getSessionId());
    }

}
