package cz.matfyz.server.job;

import cz.matfyz.server.utils.entity.Id;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionService {

    @Autowired
    private ActionRepository repository;

    public Action create(Id categoryId, String label, List<JobPayload> payloads) {
        final var action = Action.createNew(categoryId, label, payloads);
        repository.save(action);

        return action;
    }

}
