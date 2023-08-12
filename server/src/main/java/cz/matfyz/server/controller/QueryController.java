package cz.matfyz.server.controller;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.algorithms.QueryToInstance;
import cz.matfyz.querying.core.KindDefinition;
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
        List<String> jsonValues
    ) {}

    @PostMapping("/execute")
    public QueryResult executeQuery(@RequestBody QueryInput data) {
        final var categoryWrapper = schemaCategoryService.find(data.categoryId);
        final var context = new SchemaCategoryContext();
        final var category = categoryWrapper.toSchemaCategory(context);

        final var kinds = defineKinds(categoryWrapper.id, category);

        final var queryToInstance = new QueryToInstance();
        queryToInstance.input(category, data.queryString, null, kinds);
        final var result = queryToInstance.algorithm();

        return new QueryResult(result.jsonValues());
    }

    private List<KindDefinition> defineKinds(Id categoryId, SchemaCategory category) {
        return logicalModelService.findAll(categoryId).stream()
            .flatMap(logicalModel -> {
                final var database = databaseService.find(logicalModel.databaseId);
                
                return mappingService.findAll(logicalModel.id).stream().map(mappingWrapper -> {
                    final var mapping = MappingBuilder.build(category, mappingWrapper);

                    return new KindDefinition(mapping, logicalModel.databaseId.toString(), wrapperService.getControlWrapper(database));
                });
            }).toList();
    }

}
