package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.entity.datasource.DatasourceUpdate;
import cz.matfyz.server.repository.DatasourceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasourceService {

    @Autowired
    private DatasourceRepository repository;

    public DatasourceWrapper create(DatasourceInit data) {
        final var datasource = DatasourceWrapper.createNew(data);
        repository.save(datasource);

        return datasource;
    }

    public DatasourceWrapper update(Id datasourceId, DatasourceUpdate data) {
        final DatasourceWrapper datasource = repository.find(datasourceId);

        datasource.updateFrom(data);
        repository.save(datasource);

        return datasource;
    }

}

