package cz.matfyz.server.service;

import cz.matfyz.server.controller.ActionController.ActionInit;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.Action;
import cz.matfyz.server.repository.ActionRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionService {

    @Autowired
    private ActionRepository repository;

    public List<Action> findAllInCategory(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public Action find(Id id) {
        return repository.find(id);
    }

    public Action create(ActionInit init) {
        final var action = Action.createNew(init.categoryId(), init.label(), init.payload());
        repository.save(action);

        return action;
    }

    public void delete(Id id) {
        repository.delete(id);
    }

}
