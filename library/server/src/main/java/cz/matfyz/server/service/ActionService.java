package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.Action;
import cz.matfyz.server.entity.job.JobPayload;
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

    public Action create(Id categoryId, String label, List<JobPayload> payloads) {
        final var action = Action.createNew(categoryId, label, payloads);
        repository.save(action);

        return action;
    }

}
