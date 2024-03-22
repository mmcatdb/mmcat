package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.service.ModelService;

import java.io.Serializable;
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
public class ModelController {

    @Autowired
    private ModelService service;

    @GetMapping("/schema-categories/{categoryId}/models")
    public List<ModelInfo> getAllModelsInCategory(HttpSession session, @PathVariable Id categoryId) {
        return service.findAllInCategory(session, categoryId).stream().map(ModelInfo::new).toList();
    }

    @GetMapping("/models/{jobId}")
    public Model getModel(HttpSession session, @PathVariable Id jobId) {
        var model = service.findModel(session, jobId);

        if (model == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return model;
    }

    public record ModelInfo(
        Id jobId,
        String jobLabel
    ) implements Serializable {
        public ModelInfo(Model model) {
            this(model.jobId(), model.jobLabel());
        }
    }

    public record Model(
        Id jobId,
        Id categoryId,
        String jobLabel,
        String commands
    ) implements Serializable {}

}
