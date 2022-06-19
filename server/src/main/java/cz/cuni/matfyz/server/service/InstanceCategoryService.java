package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.schema.Key;

import org.springframework.stereotype.Service;
import java.util.*;
import javax.servlet.http.HttpSession;


/**
 * 
 * @author jachym.bartik
 */
@Service
public class InstanceCategoryService {

    public List<InstanceCategory> findAll(HttpSession session) {
        var store = UserStore.fromSession(session);
        return store.getAllInstances().stream().toList();
    }

    public InstanceObject findObject(HttpSession session, Key key) {
        var store = UserStore.fromSession(session);
        var defaultInstance = store.getDefaultInstace();

        return defaultInstance != null ? defaultInstance.getObject(key) : null;
    }

}
