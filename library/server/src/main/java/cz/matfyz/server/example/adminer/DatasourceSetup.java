package cz.matfyz.server.example.adminer;

import cz.matfyz.server.entity.datasource.DatasourceEntity;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.example.common.DatasourceSettings;
import cz.matfyz.server.global.Configuration.SetupProperties;
import cz.matfyz.server.service.DatasourceService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component("adminerDatasourceSetup")
class DatasourceSetup {

    private final DatasourceSettings settings;
    private final DatasourceService datasourceService;

    @Autowired
    DatasourceSetup(SetupProperties properties, DatasourceService datasourceService) {
        this.settings = new DatasourceSettings(properties, properties.adminerDatabase());
        this.datasourceService = datasourceService;
    }

    List<DatasourceEntity> createDatasources() {
        final List<DatasourceInit> inits = new ArrayList<>();

        inits.add(settings.createPostgreSQL("PostgreSQL - Adminer"));
        inits.add(settings.createMongoDB("MongoDB - Adminer"));
        inits.add(settings.createNeo4j("Neo4j - Adminer"));

        return inits.stream().map(datasourceService::create).toList();
    }

}
