package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.server.entity.Model;
import cz.cuni.matfyz.server.entity.ModelView;
import cz.cuni.matfyz.server.service.ModelService;

import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 
 * @author jachym.bartik
 */
@RestController
public class ModelController {

    @Autowired
    private ModelService service;

    @GetMapping("/models")
    public List<ModelView> getAllModels(HttpSession session) {
        return service.findAll(session).stream().map(model -> new ModelView(model)).toList();
    }

    @GetMapping("/models/{jobId}")
    public Model getInstanceObject(HttpSession session, @PathVariable Integer jobId) {
        var model = service.findModel(session, jobId);

        if (model == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        
        return model;
    }

}
