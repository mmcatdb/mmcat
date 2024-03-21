package cz.matfyz.server.service;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.instance.InstanceMorphism;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.utils.UserStore;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class InstanceCategoryService {

    public InstanceObject findObject(HttpSession session, Id categoryId, Key key) {
        var store = UserStore.fromSession(session);
        var category = store.getCategory(categoryId);

        return category != null ? category.getObject(key) : null;
    }

    public InstanceMorphism findMorphism(HttpSession session, Id categoryId, Signature signature) {
        var store = UserStore.fromSession(session);
        var category = store.getCategory(categoryId);

        return category != null ? category.getMorphism(signature) : null;
    }

}
