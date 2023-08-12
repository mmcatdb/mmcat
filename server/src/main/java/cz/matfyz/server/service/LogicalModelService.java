package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.logicalmodel.LogicalModelDetail;
import cz.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.matfyz.server.repository.LogicalModelRepository;

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

    public LogicalModelDetail findFull(Id logicalModelId) {
        var logicalModel = find(logicalModelId);
        var mappings = mappingService.findAll(logicalModel.id);
        var database = databaseService.findDatabaseWithConfiguration(logicalModel.databaseId);

        return new LogicalModelDetail(
            logicalModel.id,
            logicalModel.categoryId,
            database,
            logicalModel.label,
            mappings
        );
    }

    public List<LogicalModelDetail> findAllFull(Id categoryId) {
        return repository.findAllInCategory(categoryId).stream().map(logicalModel -> {
            var mappings = mappingService.findAll(logicalModel.id);
            var database = databaseService.findDatabaseWithConfiguration(logicalModel.databaseId);

            return new LogicalModelDetail(
                logicalModel.id,
                logicalModel.categoryId,
                database,
                logicalModel.label,
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
            init.label()
        );
    }
}
