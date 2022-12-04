package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.cuni.matfyz.server.repository.LogicalModelRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author jachym.bartik
 */
@Service
public class LogicalModelService {

    @Autowired
    private LogicalModelRepository repository;

    public List<LogicalModel> findAllInCategory(int categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public LogicalModel find(int id) {
        return repository.find(id);
    }

    public LogicalModel createNew(LogicalModelInit init) {
        Integer generatedId = repository.add(init);

        return generatedId == null ? null : new LogicalModel(
            generatedId,
            init.categoryId(),
            init.databaseId(),
            init.jsonValue()
        );
    }
}
