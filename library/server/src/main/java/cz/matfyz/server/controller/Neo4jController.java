package cz.matfyz.server.controller;

import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.server.Configuration.DatabaseProperties;
import cz.matfyz.server.Configuration.SetupProperties;
import cz.matfyz.server.repository.utils.DatabaseWrapper;
import cz.matfyz.wrapperneo4j.Neo4jProvider;
import cz.matfyz.wrapperneo4j.Neo4jProvider.Neo4jSettings;
import cz.matfyz.wrapperneo4j.Neo4jPullWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Neo4jController {

    private final Neo4jPullWrapper wrapper;

    @Autowired
    public Neo4jController(DatabaseProperties databaseProperties, SetupProperties setupProperties, DatabaseWrapper databaseWrapper) {
        String port = "3206";
        String db = "neo4j";

        Neo4jProvider neo4jProvider = new Neo4jProvider(new Neo4jSettings(
            databaseProperties.host(),
            port,
            db,
            db,
            setupProperties.password(),
            true,
            true
        ));

        this.wrapper = new Neo4jPullWrapper(neo4jProvider);
    }

    private ResponseEntity<String> getJsonResponse(JSONArray json){
        String jsonResponse = json.toString();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResponse);
    }

    @GetMapping(value = "/neo4j")
    public ResponseEntity<String> getAllNeo4j(@RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray nodes = wrapper.getNodes(limit);
        JSONArray relationships = wrapper.getRelationships(limit);
        JSONObject json = new JSONObject();

        try {
            json.put("nodes", nodes);
            json.put("relationships", relationships);
        }
        catch (JSONException e){
            throw QueryException.message("Error when getting data.");
        }

        String jsonResponse = json.toString();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResponse);
    }

    @GetMapping(value = "/neo4j/nodes")
    public ResponseEntity<String> getAllNeo4jNodes(@RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getNodes(limit);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/neo4j/node", params = {"id"})
    public ResponseEntity<String> getNeo4jRow(@RequestParam String id, @RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getNode(id, limit);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/neo4j/relationships")
    public ResponseEntity<String> getAllNeo4jRelationships(@RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getRelationships(limit);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/neo4j/relationship", params = {"id"})
    public ResponseEntity<String> getAllNeo4jRelationship(@RequestParam String id, @RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getRelationship(id, limit);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/neo4j/labels")
    public ResponseEntity<String> getAllNeo4jLabels(@RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getLabels(limit);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/neo4j/label", params = {"name"})
    public ResponseEntity<String> getNeo4jLabel(@RequestParam String name, @RequestParam(required = false, defaultValue = "50") String limit) {
        JSONArray json = wrapper.getByLabel(name, limit);

        return getJsonResponse(json);
    }
}

