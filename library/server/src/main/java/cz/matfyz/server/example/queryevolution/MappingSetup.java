package cz.matfyz.server.example.queryevolution;

import cz.matfyz.server.category.SchemaCategoryEntity;
import cz.matfyz.server.datasource.DatasourceEntity;
import cz.matfyz.tests.example.queryevolution.MongoDB;
import cz.matfyz.tests.example.queryevolution.PostgreSQL;
import cz.matfyz.server.example.common.MappingEntityBuilder;
import cz.matfyz.server.mapping.MappingEntity;
import cz.matfyz.server.mapping.MappingService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("queryEvolutionMappingSetup")
class MappingSetup {

    @Autowired
    private MappingService mappingService;

    List<MappingEntity> createMappings(List<DatasourceEntity> datasources, SchemaCategoryEntity schemaEntity, int version) {
        final var builder = new MappingEntityBuilder(datasources, schemaEntity);

        if (version == 3) {
            return builder
                .add(1, MongoDB::orders)
                .build(mappingService::create);
        }

        builder
            .add(0, PostgreSQL::customer)
            .add(0, PostgreSQL::knows)
            .add(0, PostgreSQL::product);

        if (version == 1) {
            builder.add(0, PostgreSQL::orders);
        }
        else if (version == 2) {
            builder
                .add(0, PostgreSQL::order)
                .add(0, PostgreSQL::ordered)
                .add(0, PostgreSQL::item);
        }
        else if (version == 4) {
            builder.add(1, MongoDB::order);
        }

        return builder.build(mappingService::create);
    }

}
