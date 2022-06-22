package cz.cuni.matfyz.server.utils;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.server.entity.Model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

/**
 * 
 * @author jachym.bartik
 */
public class UserStore {

    private static String USER_STORE_KEY = "USER_STORE";

    private Map<Integer, InstanceCategory> instaces = new TreeMap<>();
    private Map<Integer, Model> models = new TreeMap<>();

    public static UserStore fromSession(HttpSession session) {
        UserStore store = (UserStore) session.getAttribute(USER_STORE_KEY);

        if (store == null) {
            store = new UserStore();
            session.setAttribute(USER_STORE_KEY, store);
        }

        return store;
    }

    public void addInstance(int jobId, InstanceCategory instance) {
        instaces.put(jobId, instance);
    }

    public InstanceCategory getInstance(int jobId) {
        return instaces.get(jobId);
    }

    public Collection<InstanceCategory> getAllInstances() {
        return instaces.values();
    }

    private InstanceCategory defaultInstance = null;

    public InstanceCategory getDefaultInstace() {
        return defaultInstance;
    }
    
    public void setDefaultInstance(InstanceCategory instance) {
        this.defaultInstance = instance;
    }

    public void addModel(Model model) {
        models.put(model.jobId, model);
    }

    public Model getModel(int jobId) {
        return models.get(jobId);
    }

    public Collection<Model> getAllModels() {
        return models.values();
    }
    
}
