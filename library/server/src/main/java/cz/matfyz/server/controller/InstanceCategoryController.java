package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.instance.InstanceCategoryWrapper;
import cz.matfyz.server.service.InstanceCategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class InstanceCategoryController {

    @Autowired
    private InstanceCategoryService service;

    @GetMapping("/instances")
    public InstanceCategoryWrapper getInstanceCategory(@CookieValue("session") Id sessionId) {
        final var wrapper = service.findCategory(sessionId);
        if (wrapper == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return wrapper;
    }

}
