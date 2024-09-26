package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.service.DatasourceService;
import cz.matfyz.server.service.WrapperService;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AdminerController {

    @Autowired
    private DatasourceService datasourceService;

    @Autowired
    private WrapperService wrapperService;

    private ResponseEntity<String> getJsonResponse(JSONObject json){
        String jsonResponse = json.toString();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(jsonResponse);
    }

    @GetMapping(value = "/adminer/{db}")
    public ResponseEntity<String> getTableNames(@PathVariable Id db, @RequestParam(required = false, defaultValue = "50") String limit, @RequestParam(required = false, defaultValue = "0") String offset) {
        final var datasource = datasourceService.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var myWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();
        JSONObject json = myWrapper.getTableNames(limit, offset);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/adminer/{db}/{table}")
    public ResponseEntity<String> getTable(@PathVariable Id db, @PathVariable String table, @RequestParam(required = false, defaultValue = "50") String limit, @RequestParam(required = false, defaultValue = "0") String offset) {
        final var datasource = datasourceService.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var myWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();
        JSONObject json = myWrapper.getTable(table, limit, offset);

        return getJsonResponse(json);
    }

    @GetMapping(value = "/adminer/{db}/{table}", params = {"columnName", "columnValue", "operator"})
    public ResponseEntity<String> getRows(@PathVariable Id db, @PathVariable String table, @RequestParam String columnName, @RequestParam String columnValue, @RequestParam String operator, @RequestParam(required = false, defaultValue = "50") String limit, @RequestParam(required = false, defaultValue = "0") String offset) {
        final var datasource = datasourceService.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var myWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();
        JSONObject json = myWrapper.getRows(table, columnName, columnValue, operator, limit, offset);

        return getJsonResponse(json);
    }

}
