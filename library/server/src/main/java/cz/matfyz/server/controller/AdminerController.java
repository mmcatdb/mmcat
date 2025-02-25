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

    private static final ObjectReader filterReader = new ObjectMapper().readerFor(AdminerFilter.class);

    private void addFilterToList(String filter, List<AdminerFilter> list) throws JsonProcessingException {
        final AdminerFilter parsed = filterReader.readValue(filter);
        list.add(new AdminerFilter(parsed.propertyName(), parsed.operator(), parsed.propertyValue()));
    }

    @GetMapping(value = "/adminer/{db}/{kind}")
    public DataResponse getKind(@PathVariable Id db, @PathVariable String kind, @RequestParam(required = false, defaultValue = "") String[] filters, @RequestParam(required = false, defaultValue = "50") String limit, @RequestParam(required = false, defaultValue = "0") String offset) {
        final var datasource = datasourceRepository.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<AdminerFilter> filterList = new ArrayList<>();

        if (filters.length == 0 || (filters.length == 1 && filters[0].isEmpty())) {
            filterList = null;
        } else {
            try {
                if (filters[0].contains("{") && !filters[0].contains("}")){
                    final var filter = String.join(", ", filters);
                    addFilterToList(filter, filterList);
                } else {
                    for (final var filter : filters) {
                        addFilterToList(filter, filterList);
                    }
                }
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid filter format.");
            }
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
