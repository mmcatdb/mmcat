package cz.matfyz.server.utils;

import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.Model;

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

    private Map<Id, InstanceCategory> instaces = new TreeMap<>();
    private Map<Id, Model> models = new TreeMap<>();

    public static UserStore fromSession(HttpSession session) {
        UserStore store = (UserStore) session.getAttribute(USER_STORE_KEY);

        if (store == null) {
            store = new UserStore();
            session.setAttribute(USER_STORE_KEY, store);
        }

        return store;
    }

    public void addInstance(Id categoryId, InstanceCategory instance) {
        instaces.put(categoryId, instance);
    }

    public InstanceCategory getCategory(Id categoryId) {
        return instaces.get(categoryId);
    }

    public Set<Entry<Id, InstanceCategory>> getAllInstances() {
        return instaces.entrySet();
    }

    public void setInstance(Id categoryId, InstanceCategory instance) {
        this.instaces.put(categoryId, instance);
    }

    public void addModel(Model model) {
        models.put(model.jobId(), model);
    }

    public Model getModel(Id jobId) {
        return models.get(jobId);
    }

    public Collection<Model> getAllModels() {
        return models.values();
    }
    
}
