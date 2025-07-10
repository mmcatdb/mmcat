package cz.matfyz.server.example.queryevolution;

import cz.matfyz.server.entity.datasource.DatasourceEntity;
import cz.matfyz.server.entity.mapping.MappingEntity;
import cz.matfyz.server.entity.SchemaCategoryEntity;
import cz.matfyz.server.service.SchemaCategoryService;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;
import cz.matfyz.tests.example.queryevolution.Schema;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("queryEvolutionExampleSetup")
public class ExampleSetup {

    @Autowired
    @Qualifier("queryEvolutionDatasourceSetup")
    private DatasourceSetup datasourceSetup;

    @Autowired
    @Qualifier("queryEvolutionMappingSetup")
    private MappingSetup mappingSetup;

    @Autowired
    @Qualifier("queryEvolutionQuerySetup")
    private QuerySetup querySetup;

    public SchemaCategoryEntity setup(int version) {
        SchemaCategoryEntity schema = createSchemaCategory();
        final List<DatasourceEntity> datasources = datasourceSetup.createDatasources();

        querySetup.createQueries(schema.id());

        if (version > 1)
            schema = updateSchemaCategory(schema);

        final List<MappingEntity> mappings = mappingSetup.createMappings(datasources, schema, version);

        // // TODO jobs

        return schema;
    }

    @Autowired
    private SchemaCategoryService schemaService;

    private SchemaCategoryEntity createSchemaCategory() {
        final SchemaCategoryEntity schemaEntity = schemaService.create(Schema.schemaLabel);

        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(schemaEntity, 1);

        return schemaService.update(schemaEntity.id(), schemaUpdate);
    }

    private SchemaCategoryEntity updateSchemaCategory(SchemaCategoryEntity entity) {
        final SchemaEvolutionInit schemaUpdate = SchemaSetup.createNewUpdate(entity, 2);
        return schemaService.update(entity.id(), schemaUpdate);
    }

}
