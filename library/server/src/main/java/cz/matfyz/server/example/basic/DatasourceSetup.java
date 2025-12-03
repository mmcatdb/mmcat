package cz.matfyz.server.example.basic;

import cz.matfyz.server.datasource.DatasourceEntity;
import cz.matfyz.server.datasource.DatasourceInit;
import cz.matfyz.server.datasource.DatasourceRepository;
import cz.matfyz.server.datasource.DatasourceService;
import cz.matfyz.server.example.common.DatasourceSettings;
import cz.matfyz.server.utils.Configuration.SetupProperties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("basicDatasourceSetup")
class DatasourceSetup {

    private final DatasourceSettings settings;
    private final DatasourceService service;
    private final DatasourceRepository repository;

    @Autowired
    DatasourceSetup(SetupProperties properties, DatasourceService service, DatasourceRepository repository) {
        this.settings = new DatasourceSettings(properties, properties.basicDatabase());
        this.service = service;
        this.repository = repository;
    }

    List<DatasourceEntity> createDatasources() {
        final List<DatasourceInit> inits = new ArrayList<>();

        inits.add(settings.createPostgreSQL("PostgreSQL"));
        inits.add(settings.createMongoDB("MongoDB"));
        inits.add(settings.createNeo4j("Neo4j"));

        final List<DatasourceEntity> existingDatasources = repository.findAll();

        return inits.stream()
            .map(init -> {
                final var existing = existingDatasources.stream().filter(ed -> ed.isEqualToInit(init)).findFirst();
                return existing.isPresent()
                    ? existing.get()
                    : service.create(init);
            }).toList();
    }

}
