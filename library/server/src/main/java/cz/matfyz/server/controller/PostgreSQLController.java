package cz.matfyz.server.controller;

import cz.matfyz.server.Configuration.DatabaseProperties;
import cz.matfyz.server.Configuration.SetupProperties;
import cz.matfyz.server.repository.utils.DatabaseWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider.PostgreSQLSettings;
import cz.matfyz.wrapperpostgresql.PostgreSQLPullWrapper;

import org.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostgreSQLController {

    private final PostgreSQLPullWrapper wrapper;

    @Autowired
    public PostgreSQLController(DatabaseProperties databaseProperties, SetupProperties setupProperties, DatabaseWrapper databaseWrapper) {
        String port = "3204";

        PostgreSQLProvider postgreSqlProvider = new PostgreSQLProvider(new PostgreSQLSettings(
            databaseProperties.host(),
            port,
            setupProperties.basicDatabase(),
            setupProperties.username(),
            setupProperties.password(),
            true,
            true
        ));
        
        this.wrapper = new PostgreSQLPullWrapper(postgreSqlProvider);
    }

    private ResponseEntity<String> getJsonResponse(JSONArray json){
        String jsonResponse = json.toString();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResponse);
    }

    @GetMapping(value = "/postgre", params = {"query"})
    public ResponseEntity<String> getPostgreQuery(@RequestParam String query) {
        JSONArray json = wrapper.getQuery(query);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/postgre/all")
    public ResponseEntity<String> getPostgreTablesNames(@RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'public' LIMIT " + limit + ";");

        return getJsonResponse(json);
    }

    @GetMapping(value = "/postgre/table", params = {"name"})
    public ResponseEntity<String> getPostgreTable(@RequestParam String name, @RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getQuery("SELECT * FROM " + name + " LIMIT " + limit + ";");

        return getJsonResponse(json);
    }

    @GetMapping(value = "/postgre/table", params = {"name", "id"})
    public ResponseEntity<String> getPostgreRow(@RequestParam String name, @RequestParam String id, @RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getQuery("SELECT * FROM " + name +  " WHERE id = '" + id +"'" + " LIMIT " + limit + ";");

        return getJsonResponse(json);
    }
}

