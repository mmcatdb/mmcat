package cz.matfyz.server.example.basic;

import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.service.MappingService;
import cz.matfyz.tests.mapping.MongoDB;
import cz.matfyz.tests.mapping.Neo4j;
import cz.matfyz.tests.mapping.PostgreSQL;
import cz.matfyz.tests.mapping.TestMapping;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.evolution.Version;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MappingSetup {

    @Autowired
    MappingService mappingService;

    public List<MappingInfo> createMappings(List<LogicalModel> models, SchemaCategoryWrapper schemaWrapper) {
        version = schemaWrapper.version;
        final var schema = schemaWrapper.toSchemaCategory();

        addMapping(models.get(0), PostgreSQL.order(schema));
        addMapping(models.get(0), PostgreSQL.product(schema));
        addMapping(models.get(0), PostgreSQL.item(schema));
        addMapping(models.get(1), MongoDB.address(schema));
        addMapping(models.get(1), MongoDB.contact(schema));
        addMapping(models.get(2), Neo4j.item(schema));

        return output.stream().map(init -> mappingService.createNew(init)).toList();
    }

    private Version version;
    private List<MappingInit> output = new ArrayList<>();
    
    private void addMapping(LogicalModel model, TestMapping testMapping) {
        final Mapping mapping = testMapping.mapping();
        final var init = new MappingInit(
            model.id,
            mapping.rootObject().key(),
            mapping.primaryKey().toArray(Signature[]::new),
            mapping.kindName(),
            mapping.accessPath(),
            version
        );
        output.add(init);
    }

}
