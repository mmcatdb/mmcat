package cz.matfyz.server.instance;

import cz.matfyz.server.utils.RequestContext;

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
    public InstanceCategoryEntity getInstanceCategory() {
        return repository.find(request.getSessionId());
    }

}
