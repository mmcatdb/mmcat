package cz.matfyz.server.example.adminer;

import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.service.SchemaCategoryService;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.tests.example.adminer.Schema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("adminerExampleSetup")
public class ExampleSetup {

    @Autowired
    @Qualifier("adminerDatasourceSetup")
    private DatasourceSetup datasourceSetup;

    @Autowired
    @Qualifier("adminerMappingSetup")
    private MappingSetup mappingSetup;

    public SchemaCategoryWrapper setup() {
        final SchemaCategoryWrapper schema = createSchemaCategory();
        final List<DatasourceWrapper> datasources = datasourceSetup.createDatasources();
        final List<MappingWrapper> mappings = mappingSetup.createMappings(datasources, schema);

        return schema;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryWrapper createSchemaCategory() {
        final SchemaCategoryWrapper schemaWrapper = schemaService.create(Schema.schemaLabel);

        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(schemaWrapper);

        return schemaService.update(schemaWrapper.id(), schemaUpdate);
    }

}
