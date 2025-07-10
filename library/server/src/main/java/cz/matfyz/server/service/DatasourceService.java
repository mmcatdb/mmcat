package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceEntity;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.entity.datasource.DatasourceUpdate;
import cz.matfyz.server.repository.DatasourceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatasourceService {

    @Autowired
    private DatasourceRepository repository;

    public DatasourceEntity create(DatasourceInit data) {
        final var datasource = DatasourceEntity.createNew(data);
        repository.save(datasource);

        return datasource;
    }

    public DatasourceEntity update(Id datasourceId, DatasourceUpdate data) {
        final DatasourceEntity datasource = repository.find(datasourceId);

        datasource.updateFrom(data);
        repository.save(datasource);

        return datasource;
    }

}

