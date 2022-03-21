package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.core.instance.InstanceCategory;

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

}
