package cz.matfyz.server.example.basic;

import cz.matfyz.server.entity.MappingEntity;
import cz.matfyz.server.entity.SchemaCategoryEntity;
import cz.matfyz.server.entity.datasource.DatasourceEntity;
import cz.matfyz.server.service.MappingService;
import cz.matfyz.tests.example.basic.MongoDB;
import cz.matfyz.tests.example.basic.Neo4j;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.server.example.common.MappingEntityBuilder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("basicMappingSetup")
class MappingSetup {

    @Autowired
    private MappingService mappingService;

    List<MappingEntity> createMappings(List<DatasourceEntity> datasources, SchemaCategoryEntity schemaEntity) {
        return new MappingEntityBuilder(datasources, schemaEntity)
            .add(0, PostgreSQL::order)
            .add(0, PostgreSQL::product)
            .add(0, PostgreSQL::item)
            .add(1, MongoDB::address)
            .add(1, MongoDB::tagSet)
            .add(1, MongoDB::contact)
            .add(1, MongoDB::customer)
            .add(1, MongoDB::note)
            .add(2, Neo4j::item)
            .build(mappingService::create);
    }

}
