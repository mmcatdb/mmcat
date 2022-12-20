package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelFull;
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

    @Autowired
    private MappingService mappingService;

    @Autowired
    private DatabaseService databaseService;

    public List<LogicalModel> findAll(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public LogicalModel find(Id logicalModelId) {
        return repository.find(logicalModelId);
    }

    public LogicalModelFull findFull(Id logicalModelId) {
        var logicalModel = find(logicalModelId);
        var mappings = mappingService.findAll(logicalModel.id);
        var database = databaseService.findDatabaseWithConfiguration(logicalModel.databaseId);

        return new LogicalModelFull(
            logicalModel.id,
            logicalModel.categoryId,
            database,
            logicalModel.jsonValue,
            mappings
        );
    }

    public List<LogicalModelFull> findAllFull(Id categoryId) {
        return repository.findAllInCategory(categoryId).stream().map(logicalModel -> {
            var mappings = mappingService.findAll(logicalModel.id);
            var database = databaseService.findDatabaseWithConfiguration(logicalModel.databaseId);

            return new LogicalModelFull(
                logicalModel.id,
                logicalModel.categoryId,
                database,
                logicalModel.jsonValue,
                mappings
            );
        }).toList();
    }

    public LogicalModel createNew(LogicalModelInit init) {
        Id generatedId = repository.add(init);

        return generatedId == null ? null : new LogicalModel(
            generatedId,
            init.categoryId(),
            init.databaseId(),
            init.jsonValue()
        );
    }
}
