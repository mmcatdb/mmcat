package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.service.WrapperService;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.adminer.KindNamesResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@RestController
public class AdminerController {

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private WrapperService wrapperService;

    /**
     * Retrieves the available kind names from a given datasource.
     *
     * @param db The ID of the datasource.
     * @param limit The maximum number of kind names to return. Defaults to 50.
     * @param offset The offset from which to start retrieving kind names. Defaults to 0.
     * @return A {@link KindNamesResponse} containing the kind names.
     * @throws ResponseStatusException if the datasource is not found.
     */
    @GetMapping(value = "/adminer/{db}")
    public KindNamesResponse getKindNames(
        @PathVariable Id db,
        @RequestParam(required = false, defaultValue = "50") String limit,
        @RequestParam(required = false, defaultValue = "0") String offset
        ) {
        final var datasource = datasourceRepository.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getKindNames(limit, offset);
    }

    /**
     * Retrieves the data from a specific kind with optional filtering.
     *
     * @param db The ID of the datasource.
     * @param kindName The name of the kind.
     * @param filters A JSON array string representing the filters to apply.
     * @param limit The maximum number of records to return. Defaults to 50.
     * @param offset The offset from which to start retrieving records. Defaults to 0.
     * @return A {@link DataResponse} containing the data.
     * @throws ResponseStatusException if the datasource is not found.
     * @throws IllegalArgumentException if the filter format is invalid.
     */
    @GetMapping(value = "/adminer/{db}/kind")
    public DataResponse getKind(
        @PathVariable Id db,
        @RequestParam(required = false, defaultValue = "") String kindName,
        @RequestParam(required = false, defaultValue = "") String filters,
        @RequestParam(required = false, defaultValue = "50") String limit,
        @RequestParam(required = false, defaultValue = "0") String offset
        ) {
        final var datasource = datasourceRepository.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<AdminerFilter> filterList = new ArrayList<>();

        if (!filters.isEmpty() && !filters.equals("[]")) {
            try {
                List<AdminerFilter> allFilters = new ObjectMapper()
                    .readerForListOf(AdminerFilter.class)
                    .readValue(filters);

                for (AdminerFilter filter : allFilters) {
                    if (!filter.propertyName().isEmpty() && !filter.operator().isEmpty() && !filter.propertyValue().isEmpty()) {
                        filterList.add(filter);
                    }
                }
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid filter format.");
            }
        }

        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getKind(kindName, limit, offset, filterList);
    }

    /**
     * Retrieves the references (foreign key-like relationships) for a given kind in the specified datasource.
     *
     * @param db The ID of the datasource.
     * @param kindName The name of the kind for which to retrieve references.
     * @return A {@link List} of {@link Reference} representing the relationships.
     * @throws ResponseStatusException if the datasource is not found.
     */
    @GetMapping(value = "/adminer/{db}/references")
    public List<Reference> getReferences(
        @PathVariable Id db,
        @RequestParam(required = true) String kindName
        ) {
        final var datasource = datasourceRepository.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getReferences(db.toString(), kindName);
    }

    /**
     * Executes a custom query string on the specified datasource and returns the result.
     *
     * @param db The ID of the datasource.
     * @param query The query to be executed.
     * @return A {@link DataResponse} containing the result of the query.
     * @throws ResponseStatusException if the datasource is not found.
     */
    @GetMapping(value = "/adminer/{db}/query")
    public DataResponse getQueryResult(
        @PathVariable Id db,
        @RequestParam(required = true) String query
        ) {
        final var datasource = datasourceRepository.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getQueryResult(new StringQuery(query));
    }

}
