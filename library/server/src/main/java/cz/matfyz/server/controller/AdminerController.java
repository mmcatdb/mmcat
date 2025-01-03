package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.service.WrapperService;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.ForeignKey;
import cz.matfyz.core.adminer.KindNameResponse;
import cz.matfyz.core.record.AdminerFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping(value = "/adminer/{db}/{kind}", params = {"filters"})
    public DataResponse getRows(@PathVariable Id db, @PathVariable String kind, @RequestParam String filters, @RequestParam(required = false, defaultValue = "50") String limit, @RequestParam(required = false, defaultValue = "0") String offset) {
        final var datasource = datasourceRepository.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<AdminerFilter> filterList = new ArrayList<>();

        for (int indexOpen = filters.indexOf('(');
            indexOpen >= 0;
            indexOpen = filters.indexOf('(', indexOpen + 1)) {
            int indexClose = filters.indexOf(')', indexOpen + 1);

            String filter = filters.substring(indexOpen + 1, indexClose);
            String[] filterParams = filter.split(",");

            if (filterParams.length == 3) {
                filterList.add(new AdminerFilter(filterParams[0], filterParams[1], filterParams[2]));
            }
        }

        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getKind(kind, limit, offset, filterList);
    }

    @GetMapping(value = "/adminer/{db}/{kind}/foreignkeys")
    public List<ForeignKey> getForeignKeys(@PathVariable Id db, @PathVariable String kind) {
        final var datasource = datasourceRepository.find(db);

        if (datasource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        final var pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();

        return pullWrapper.getForeignKeys(kind);
    }

}
