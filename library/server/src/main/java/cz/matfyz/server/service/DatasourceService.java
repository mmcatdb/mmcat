package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.entity.datasource.DatasourceUpdate;
import cz.matfyz.server.repository.DatasourceRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DatasourceService {

    @Autowired
    private DatasourceRepository repository;

    public DatasourceWrapper find(Id datasourceId) {
        return repository.find(datasourceId);
    }

    public List<DatasourceWrapper> findAll() {
        return repository.findAll();
    }

    public List<DatasourceWrapper> findAllInCategory(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public DatasourceWrapper createNew(DatasourceInit data) {
        final var datasource = DatasourceWrapper.createNew(data);
        repository.save(datasource);
        
        return datasource;
    }

    public DatasourceWrapper update(Id datasourceId, DatasourceUpdate data) {
        DatasourceWrapper datasource = repository.find(datasourceId);
        if (datasource == null)
            return null;

        datasource.updateFrom(data);
        repository.save(datasource);

        return datasource;
    }

    public boolean delete(Id datasourceId) {
        return repository.delete(datasourceId);
    }

}

