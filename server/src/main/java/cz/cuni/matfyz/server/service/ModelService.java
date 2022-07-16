package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.Job;
import cz.cuni.matfyz.server.entity.Model;
import cz.cuni.matfyz.server.utils.UserStore;

import org.springframework.stereotype.Service;
import java.util.*;
import javax.servlet.http.HttpSession;


/**
 * 
 * @author jachym.bartik
 */
@Service
public class ModelService {

    public List<Model> findAllInCategory(HttpSession session, int schemaId) {
        var store = UserStore.fromSession(session);
        return store.getAllModels().stream().filter(model -> model.schemaId == schemaId).toList();
    }

    public Model findModel(HttpSession session, int jobId) {
        var store = UserStore.fromSession(session);
        return store.getModel(jobId);
    }

    public Model createNew(UserStore store, Job job, String jobName, String commands) {
        var model = new Model(job.id, job.schemaId, jobName, commands);
        store.addModel(model);

        return model;
    }

}
