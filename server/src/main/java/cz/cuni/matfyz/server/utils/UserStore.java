package cz.cuni.matfyz.server.utils;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.server.entity.Model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.http.HttpSession;

/**
 * @author jachym.bartik
 */
public class UserStore implements Serializable {

    private static final String USER_STORE_KEY = "USER_STORE";

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

    public void addInstance(int schemaId, InstanceCategory instance) {
        instaces.put(schemaId, instance);
    }

    public InstanceCategory getCategory(int schemaId) {
        return instaces.get(schemaId);
    }

    public Set<Entry<Integer, InstanceCategory>> getAllInstances() {
        return instaces.entrySet();
    }

    public void setInstance(int schemaId, InstanceCategory instance) {
        this.instaces.put(schemaId, instance);
    }

    public void addModel(Model model) {
        models.put(model.jobId(), model);
    }

    public Model getModel(int jobId) {
        return models.get(jobId);
    }

    public Collection<Model> getAllModels() {
        return models.values();
    }
    
}
