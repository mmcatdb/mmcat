package cz.cuni.matfyz.server.utils;

import cz.cuni.matfyz.core.instance.InstanceCategory;

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
    
}
