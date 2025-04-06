package cz.matfyz.server.example.queryevolution;

import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.example.common.DatasourceSettings;
import cz.matfyz.server.global.Configuration.SetupProperties;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.service.DatasourceService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("queryEvolutionDatasourceSetup")
class DatasourceSetup {

    private final DatasourceSettings settings;
    private final DatasourceService service;
    private final DatasourceRepository repository;

    @Autowired
    DatasourceSetup(SetupProperties properties, DatasourceService service, DatasourceRepository repository) {
        this.settings = new DatasourceSettings(properties, properties.queryEvolutionDatabase());
        this.service = service;
        this.repository = repository;
    }

    List<DatasourceWrapper> createDatasources() {
        final List<DatasourceInit> inits = new ArrayList<>();

        inits.add(settings.createPostgreSQL("PostgreSQL"));
        inits.add(settings.createMongoDB("MongoDB"));

        final List<DatasourceWrapper> existingDatasources = repository.findAll();

        return inits.stream()
            .map(init -> {
                final var existing = existingDatasources.stream().filter(ed -> ed.isEqualToInit(init)).findFirst();
                return existing.isPresent()
                    ? existing.get()
                    : service.create(init);
            }).toList();
    }

}
