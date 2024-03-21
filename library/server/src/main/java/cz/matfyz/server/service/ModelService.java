package cz.matfyz.server.service;

import cz.matfyz.server.controller.ModelController.Model;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.utils.UserStore;

import java.util.List;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;


/**
 * @author jachym.bartik
 */
@Service
public class ModelService {

    public List<Model> findAllInCategory(HttpSession session, Id categoryId) {
        var store = UserStore.fromSession(session);
        return store.getAllModels().stream().filter(model -> model.categoryId().equals(categoryId)).toList();
    }

    public Model findModel(HttpSession session, Id jobId) {
        var store = UserStore.fromSession(session);
        return store.getModel(jobId);
    }

    public Model createNew(UserStore store, Job job, Run run, String jobLabel, String commands) {
        var model = new Model(job.id, run.categoryId, jobLabel, commands);
        store.addModel(model);

        return model;
    }

}
