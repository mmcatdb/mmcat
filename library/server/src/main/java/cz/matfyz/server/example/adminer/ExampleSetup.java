package cz.matfyz.server.example.adminer;

import cz.matfyz.server.entity.datasource.DatasourceEntity;
import cz.matfyz.server.entity.mapping.MappingEntity;
import cz.matfyz.server.entity.SchemaCategoryEntity;
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

    public SchemaCategoryEntity setup() {
        final SchemaCategoryEntity schema = createSchemaCategory();
        final List<DatasourceEntity> datasources = datasourceSetup.createDatasources();
        final List<MappingEntity> mappings = mappingSetup.createMappings(datasources, schema);

        return schema;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryEntity createSchemaCategory() {
        final SchemaCategoryEntity schemaEntity = schemaService.create(Schema.schemaLabel);

        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(schemaEntity);

        return schemaService.update(schemaEntity.id(), schemaUpdate);
    }

}
