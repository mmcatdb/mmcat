package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.server.utils.UserStore;

import java.util.List;
import java.util.Map.Entry;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class InstanceCategoryService {

    public List<Entry<Integer, InstanceCategory>> findAll(HttpSession session) {
        var store = UserStore.fromSession(session);
        return store.getAllInstances().stream().toList();
    }

    public InstanceObject findObject(HttpSession session, int schemaId, Key key) {
        var store = UserStore.fromSession(session);
        var instance = store.getInstance(schemaId);

        return instance != null ? instance.getObject(key) : null;
    }

}
