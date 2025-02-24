package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.service.WrapperService;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.adminer.KindNameResponse;
import cz.matfyz.core.record.AdminerFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.util.ArrayList;
import java.util.List;

@RestController
public class AdminerController {

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private WrapperService wrapperService;

    @GetMapping(value = "/adminer/{db}")
    public KindNameResponse getKindNames(@PathVariable Id db, @RequestParam(required = false, defaultValue = "50") String limit, @RequestParam(required = false, defaultValue = "0") String offset) {
        final var datasource = datasourceRepository.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getKindNames(limit, offset);
    }

    @GetMapping(value = "/adminer/{db}/{kind}")
    public DataResponse getKind(@PathVariable Id db, @PathVariable String kind, @RequestParam(required = false, defaultValue = "50") String limit, @RequestParam(required = false, defaultValue = "0") String offset) {
        final var datasource = datasourceRepository.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getKind(kind, limit, offset, null);
    }

    private static final ObjectReader filterReader = new ObjectMapper().readerFor(AdminerFilter.class);

    @GetMapping(value = "/adminer/{db}/{kind}", params = {"filters"})
    public DataResponse getRows(@PathVariable Id db, @PathVariable String kind, @RequestParam String[] filters, @RequestParam(required = false, defaultValue = "50") String limit, @RequestParam(required = false, defaultValue = "0") String offset) {
        final var datasource = datasourceRepository.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<AdminerFilter> filterList = new ArrayList<>();

        try {
            if (filters.length == 3 && filters[0].contains("{") && !filters[0].contains("}")){
                final var filter = filters[0] + ", " + filters[1] + ", " + filters[2];
                final AdminerFilter parsed = filterReader.readValue(filter);
                filterList.add(new AdminerFilter(parsed.propertyName(), parsed.operator(), parsed.propertyValue()));
            } else {
                for (final var filter : filters) {
                    final AdminerFilter parsed = filterReader.readValue(filter);
                    filterList.add(new AdminerFilter(parsed.propertyName(), parsed.operator(), parsed.propertyValue()));
                }
            }
        } catch (JsonProcessingException e) {
            System.out.println("Filters cannot be parsed.");
        }

        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getKind(kind, limit, offset, filterList);
    }

    @GetMapping(value = "/adminer/{db}/{kind}/references")
    public List<Reference> getReferences(@PathVariable Id db, @PathVariable String kind) {
        final var datasource = datasourceRepository.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getReferences(db.toString(), kind);
    }

}
