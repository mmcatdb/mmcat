package cz.matfyz.server.example.adminer;

import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.service.MappingService;
import cz.matfyz.tests.example.adminer.MongoDB;
import cz.matfyz.tests.example.adminer.Neo4j;
import cz.matfyz.tests.example.adminer.PostgreSQL;
import cz.matfyz.server.example.common.MappingWrapperBuilder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("adminerMappingSetup")
class MappingSetup {

    @Autowired
    private MappingService mappingService;

    List<MappingWrapper> createMappings(List<DatasourceWrapper> datasources, SchemaCategoryWrapper schemaWrapper) {
        return new MappingWrapperBuilder(datasources, schemaWrapper)
            .add(0, PostgreSQL::user)
            .add(0, PostgreSQL::comment)
            .add(0, PostgreSQL::businessHours)
            .add(1, MongoDB::business)
            .add(2, Neo4j::user)
            .add(2, Neo4j::friend)
            .build(mappingService::create);
    }

}
