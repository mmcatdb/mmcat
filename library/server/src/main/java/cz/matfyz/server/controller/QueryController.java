package cz.matfyz.server.controller;

import cz.matfyz.abstractwrappers.database.Database;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.abstractwrappers.queryresult.ResultList;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.algorithms.QueryToInstance;
import cz.matfyz.server.builder.MappingBuilder;
import cz.matfyz.server.builder.SchemaCategoryContext;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.service.DatabaseService;
import cz.matfyz.server.service.LogicalModelService;
import cz.matfyz.server.service.MappingService;
import cz.matfyz.server.service.SchemaCategoryService;
import cz.matfyz.server.service.WrapperService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jachym.bartik
 */
@RestController
public class QueryController {

    @Autowired
    private SchemaCategoryService schemaCategoryService;

    @Autowired
    private LogicalModelService logicalModelService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private MappingService mappingService;

    
    public static record QueryInput(
        Id categoryId,
        String queryString
    ) {}

    public static record QueryResult(
        List<String> rows
    ) {}

    @PostMapping("/execute")
    public QueryResult executeQuery(@RequestBody QueryInput data) {
        final var categoryWrapper = schemaCategoryService.find(data.categoryId);
        final var context = new SchemaCategoryContext();
        final var category = categoryWrapper.toSchemaCategory(context);

        final var kinds = defineKinds(categoryWrapper.id, category);

        final var queryToInstance = new QueryToInstance();
        queryToInstance.input(category, data.queryString, kinds);
        final ResultList result = queryToInstance.algorithm();

        return new QueryResult(result.toJsonArray());
    }

    private List<Kind> defineKinds(Id categoryId, SchemaCategory category) {
        return logicalModelService.findAll(categoryId).stream()
            .flatMap(logicalModel -> {
                final var databaseEntity = databaseService.find(logicalModel.databaseId);
                final var builder = new Database.Builder();
                
                mappingService.findAll(logicalModel.id).forEach(mappingWrapper -> {
                    final var mapping = MappingBuilder.build(category, mappingWrapper);
                    builder.mapping(mapping);
                });

                final var database = builder.build(databaseEntity.type, wrapperService.getControlWrapper(databaseEntity), databaseEntity.id.toString());
                return database.kinds.stream();
            }).toList();
    }

}
