package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.service.DatasourceService;
import cz.matfyz.server.service.WrapperService;

import org.json.JSONArray;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AdminerController {

    @Autowired
    private DatasourceService datasourceService;

    @Autowired
    private WrapperService wrapperService;

    private ResponseEntity<String> getJsonResponse(JSONArray json){
        String jsonResponse = json.toString();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResponse);
    }

    @GetMapping(value = "/adminer/{db}")
    public ResponseEntity<String> getTableNames(@PathVariable Id db, @RequestParam(required = false, defaultValue = "50") String limit) {
        final var datasource = datasourceService.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        
        final var myWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();
        JSONArray json = myWrapper.getTableNames(limit);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/adminer/{db}/table", params = {"name"})
    public ResponseEntity<String> getTable(@PathVariable Id db, @RequestParam String name, @RequestParam(required = false, defaultValue = "50") String limit) {
        final var datasource = datasourceService.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var myWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();
        JSONArray json = myWrapper.getTable(name, limit);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/adminer/{db}/row", params = {"table", "id"})
    public ResponseEntity<String> getRow(@PathVariable Id db, @RequestParam String table, @RequestParam String id, @RequestParam(required = false, defaultValue = "50") String limit) {
        final var datasource = datasourceService.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        
        final var myWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();
        JSONArray json = myWrapper.getRow(table, id, limit);

        return getJsonResponse(json);
    }
    
}
