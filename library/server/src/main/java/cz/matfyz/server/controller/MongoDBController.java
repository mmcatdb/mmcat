package cz.matfyz.server.controller;

import cz.matfyz.server.Configuration.DatabaseProperties;
import cz.matfyz.server.Configuration.SetupProperties;
import cz.matfyz.server.repository.utils.DatabaseWrapper;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrappermongodb.MongoDBProvider.MongoDBSettings;
import cz.matfyz.wrappermongodb.MongoDBPullWrapper;

import org.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MongoDBController {

    private final MongoDBPullWrapper wrapper;

    @Autowired
    public MongoDBController(DatabaseProperties databaseProperties, SetupProperties setupProperties, DatabaseWrapper databaseWrapper) {
        String port = "3205";
        String authDatabase = "admin";

        MongoDBProvider mongoDBProvider = new MongoDBProvider(new MongoDBSettings(
            databaseProperties.host(),
            port,
            authDatabase,
            setupProperties.basicDatabase(),
            setupProperties.username(),
            setupProperties.password(),
            true,
            true
        ));
        
        this.wrapper = new MongoDBPullWrapper(mongoDBProvider);
    }

    private ResponseEntity<String> getJsonResponse(JSONArray json){
        String jsonResponse = json.toString();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResponse);
    }

    @GetMapping(value = "/mongo")
    public ResponseEntity<String> getMongoTablesNames(@RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getTablesNames(limit);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/mongo/table", params = {"name"})
    public ResponseEntity<String> getMongoTable(@RequestParam String name, @RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getTable(name, limit);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/mongo/table", params = {"name", "id"})
    public ResponseEntity<String> getMongoRow(@RequestParam String name, @RequestParam String id, @RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getRow(name, id, limit);

        return getJsonResponse(json);
    }
}

