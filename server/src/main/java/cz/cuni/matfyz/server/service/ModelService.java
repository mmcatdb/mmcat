package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.Job;
import cz.cuni.matfyz.server.entity.Model;
import cz.cuni.matfyz.server.utils.UserStore;

import java.util.List;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;


/**
 * @author jachym.bartik
 */
@Service
public class ModelService {

    public List<Model> findAllInCategory(HttpSession session, int categoryId) {
        var store = UserStore.fromSession(session);
        return store.getAllModels().stream().filter(model -> model.categoryId() == categoryId).toList();
    }

    public Model findModel(HttpSession session, int jobId) {
        var store = UserStore.fromSession(session);
        return store.getModel(jobId);
    }

    public Model createNew(UserStore store, Job job, String jobLabel, String commands) {
        var model = new Model(job.id, job.categoryId, jobLabel, commands);
        store.addModel(model);

        return model;
    }

}
