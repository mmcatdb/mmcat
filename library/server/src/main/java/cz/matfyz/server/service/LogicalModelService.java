package cz.matfyz.server.service;

import cz.matfyz.server.controller.LogicalModelController.LogicalModelDetail;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.matfyz.server.repository.LogicalModelRepository;
import cz.matfyz.server.repository.LogicalModelRepository.LogicalModelWithDatabase;

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

    public List<LogicalModelWithDatabase> findAll(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public LogicalModelWithDatabase find(Id logicalModelId) {
        return repository.find(logicalModelId);
    }

    public LogicalModelDetail findFull(Id logicalModelId) {
        final var model = find(logicalModelId);
        final var mappings = mappingService.findAll(logicalModelId);

        return LogicalModelDetail.fromEntities(
            model.logicalModel(),
            databaseService.getDatabaseConfiguration(model.database()),
            mappings
        );
    }

    public List<LogicalModelDetail> findAllFull(Id categoryId) {
        return repository.findAllInCategory(categoryId).stream().map(model -> {
            final var mappings = mappingService.findAll(model.logicalModel().id);

            return LogicalModelDetail.fromEntities(
                model.logicalModel(),
                databaseService.getDatabaseConfiguration(model.database()),
                mappings
            );
        }).toList();
    }

    public LogicalModelWithDatabase createNew(LogicalModelInit init) {
        final var database = databaseService.find(init.databaseId());
        final Id generatedId = repository.add(init);
        final var logicalModel = new LogicalModel(
            generatedId,
            init.categoryId(),
            init.databaseId(),
            init.label()
        );

        return new LogicalModelWithDatabase(logicalModel, database);
    }
    
}
