package cz.matfyz.server.example.basic;

import cz.matfyz.server.configuration.SetupProperties;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.example.common.DatasourceSettings;
import cz.matfyz.server.service.DatasourceService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("basicDatasourceSetup")
class DatasourceSetup {

    private final DatasourceSettings settings;
    private final DatasourceService datasourceService;

    @Autowired
    DatasourceSetup(SetupProperties properties, DatasourceService datasourceService) {
        this.settings = new DatasourceSettings(properties, properties.basicDatabase());
        this.datasourceService = datasourceService;
    }

    List<DatasourceWrapper> createDatasources() {
        final List<DatasourceInit> inits = new ArrayList<>();

        inits.add(settings.createPostgreSQL("PostgreSQL - Basic"));
        inits.add(settings.createMongoDB("MongoDB - Basic"));
        inits.add(settings.createNeo4j("Neo4j - Basic"));

        return inits.stream().map(datasourceService::createNew).toList();
    }

}
