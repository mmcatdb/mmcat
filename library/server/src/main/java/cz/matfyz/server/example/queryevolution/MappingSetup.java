package cz.matfyz.server.example.queryevolution;

import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.service.MappingService;
import cz.matfyz.tests.example.queryevolution.PostgreSQL;
import cz.matfyz.server.example.common.MappingBuilder;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("queryEvolutionMappingSetup")
class MappingSetup {

    @Autowired
    private MappingService mappingService;

    List<MappingInfo> createMappings(List<LogicalModel> models, SchemaCategoryWrapper schemaWrapper) {
        return new MappingBuilder(models, schemaWrapper)
            .add(0, PostgreSQL::customer)
            .add(0, PostgreSQL::knows)
            .add(0, PostgreSQL::product)
            .add(0, PostgreSQL::order1)
            // .add(0, PostgreSQL::order2)
            // .add(0, PostgreSQL::item)
            // .add(0, PostgreSQL::ordered)
            // .add(1, MongoDB::address)
            // .add(1, MongoDB::contact)
            .build(mappingService::createNew);
    }

}
