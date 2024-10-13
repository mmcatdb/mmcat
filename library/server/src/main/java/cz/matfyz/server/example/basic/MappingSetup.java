package cz.matfyz.server.example.basic;

import cz.matfyz.server.entity.LogicalModel;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.service.MappingService;
import cz.matfyz.tests.example.basic.MongoDB;
import cz.matfyz.tests.example.basic.Neo4j;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.server.example.common.MappingBuilder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("basicMappingSetup")
class MappingSetup {

    @Autowired
    private MappingService mappingService;

    List<MappingWrapper> createMappings(List<LogicalModel> models, SchemaCategoryWrapper schemaWrapper) {
        return new MappingBuilder(models, schemaWrapper)
            .add(0, PostgreSQL::order)
            .add(0, PostgreSQL::product)
            .add(0, PostgreSQL::item)
            .add(1, MongoDB::address)
            .add(1, MongoDB::tag)
            .add(1, MongoDB::contact)
            .add(1, MongoDB::customer)
            .add(1, MongoDB::note)
            .add(2, Neo4j::item)
            .build(mappingService::create);
    }

}
