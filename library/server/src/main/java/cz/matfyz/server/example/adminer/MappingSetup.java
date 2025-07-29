package cz.matfyz.server.example.adminer;

import cz.matfyz.server.entity.MappingEntity;
import cz.matfyz.server.entity.SchemaCategoryEntity;
import cz.matfyz.server.entity.datasource.DatasourceEntity;
import cz.matfyz.server.service.MappingService;
import cz.matfyz.tests.example.adminer.MongoDB;
import cz.matfyz.tests.example.adminer.Neo4j;
import cz.matfyz.tests.example.adminer.PostgreSQL;
import cz.matfyz.server.example.common.MappingEntityBuilder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("adminerMappingSetup")
class MappingSetup {

    @Autowired
    private MappingService mappingService;

    List<MappingEntity> createMappings(List<DatasourceEntity> datasources, SchemaCategoryEntity schemaEntity) {
        return new MappingEntityBuilder(datasources, schemaEntity)
            .add(0, PostgreSQL::user)
            .add(0, PostgreSQL::comment)
            .add(0, PostgreSQL::businessHours)
            .add(1, MongoDB::business)
            .add(2, Neo4j::user)
            .add(2, Neo4j::friend)
            .build(mappingService::create);
    }

}
