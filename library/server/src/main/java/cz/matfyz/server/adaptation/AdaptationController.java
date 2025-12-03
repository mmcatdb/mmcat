package cz.matfyz.server.adaptation;

import cz.matfyz.server.utils.entity.Id;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdaptationController {

    @Autowired
    private AdaptationRepository repository;

    @Autowired
    private AdaptationService service;

    @GetMapping("/schema-categories/{categoryId}/adaptation")
    public @Nullable Adaptation getAdaptationForCategory(@PathVariable Id categoryId) {
        return repository.tryFindByCategory(categoryId);
    }

    @PostMapping("/schema-categories/{categoryId}/adaptation")
    public Adaptation createAdaptationForCategory(@PathVariable Id categoryId) {
        return service.createForCategory(categoryId);
    }

    @PostMapping("/adaptations/{adaptationId}/start")
    public void startAdaptation(@PathVariable Id adaptationId) {
        // TODO
    }

}
