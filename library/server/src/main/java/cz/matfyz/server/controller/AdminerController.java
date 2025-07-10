package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.service.WrapperService;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.Reference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * Retrieves the available kind names from the given datasource.
     */
    @GetMapping(value = "/adminer/{datasourceId}")
    public List<String> getKindNames(@PathVariable Id datasourceId) {
        final var datasource = datasourceRepository.find(datasourceId);
        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getKindNames();
    }

    /**
     * Retrieves the data from a specific kind with optional filtering.
     *
     * @param datasourceId The ID of the datasource.
     * @param kindName The name of the kind.
     * @param filters A JSON array string representing the filters to apply.
     * @param limit The maximum number of records to return. Defaults to 50.
     * @param offset The offset from which to start retrieving records. Defaults to 0.
     */
    @GetMapping(value = "/adminer/{datasourceId}/kind")
    public DataResponse getRecords(
        @PathVariable Id datasourceId,
        @RequestParam String kindName,
        @RequestParam(defaultValue = "") String filtersString,
        @RequestParam(defaultValue = "50") String limitString,
        @RequestParam(defaultValue = "0") String offsetString
    ) {
        final int limit = Integer.parseInt(limitString);
        final int offset = Integer.parseInt(offsetString);

        final var datasource = datasourceRepository.find(datasourceId);

        List<AdminerFilter> filters = new ArrayList<>();

        if (!filtersString.isEmpty() && !filtersString.equals("[]")) {
            try {
                List<AdminerFilter> allFilters = new ObjectMapper()
                    .readerForListOf(AdminerFilter.class)
                    .readValue(filtersString);

                for (AdminerFilter filter : allFilters) {
                    if (!filter.propertyName().isEmpty() && !filter.operator().isEmpty())
                        filters.add(filter);
                }
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid filter format.");
            }
        }

        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getRecords(kindName, limit, offset, filters);
    }

    /**
     * Retrieves the references (foreign key-like relationships) for the given kind in the specified datasource.
     *
     * @param datasourceId The ID of the datasource.
     * @param kindName The name of the kind for which to retrieve references.
     */
    @GetMapping(value = "/adminer/{datasourceId}/references")
    public List<Reference> getReferences(@PathVariable Id datasourceId, @RequestParam String kindName) {
        final var datasource = datasourceRepository.find(datasourceId);
        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getReferences(datasourceId.toString(), kindName);
    }

    /**
     * Executes a custom query string on the specified datasource and returns the result.
     *
     * @param datasourceId The ID of the datasource.
     * @param query The query to be executed.
     */
    @GetMapping(value = "/adminer/{datasourceId}/query")
    public DataResponse getQueryResult(@PathVariable Id datasourceId, @RequestParam String query) {
        final var datasource = datasourceRepository.find(datasourceId);
        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getQueryResult(new StringQuery(query));
    }

}
