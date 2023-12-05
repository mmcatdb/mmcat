package cz.matfyz.server.example.queryevolution;

import cz.matfyz.server.configuration.SetupProperties;
import cz.matfyz.server.entity.database.DatabaseEntity;
import cz.matfyz.server.entity.database.DatabaseInit;
import cz.matfyz.server.example.common.DatabaseSettings;
import cz.matfyz.server.service.DatabaseService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("queryEvolutionDatabaseSetup")
class DatabaseSetup {
    
    private final DatabaseSettings settings;
    private final DatabaseService databaseService;

    @Autowired
    DatabaseSetup(SetupProperties properties, DatabaseService databaseService) {
        this.settings = new DatabaseSettings(properties, properties.queryEvolutionDatabase());
        this.databaseService = databaseService;
    }

    List<DatabaseEntity> createDatabases() {
        final List<DatabaseInit> inits = new ArrayList<>();

        inits.add(settings.createPostgreSQL("PostgreSQL"));
        inits.add(settings.createMongoDB("MongoDB"));
        
        return inits.stream().map(databaseService::createNew).toList();
    }

}
