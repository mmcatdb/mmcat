package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DataSourceConfiguration;
import cz.matfyz.server.entity.datasource.DataSourceEntity;
import cz.matfyz.server.entity.datasource.DataSourceInit;
import cz.matfyz.server.entity.datasource.DataSourceUpdate;
import cz.matfyz.server.entity.datasource.DataSourceWithConfiguration;
import cz.matfyz.server.repository.DataSourceRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author jachym.bartik
 */
@Service
public class DataSourceService {

    @Autowired
    private DataSourceRepository repository;

    @Autowired
    private WrapperService wrapperService;

    public DataSourceEntity find(Id dataSourceId) {
        return repository.find(dataSourceId);
    }

    public List<DataSourceEntity> findAll() {
        return repository.findAll();
    }

    public List<DataSourceEntity> findAllInCategory(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public DataSourceEntity createNew(DataSourceInit data) {
        var dataSource = new DataSourceEntity(null, data);
        return repository.save(dataSource);
    }

    public DataSourceEntity update(Id dataSourceId, DataSourceUpdate data) {
        DataSourceEntity dataSource = repository.find(dataSourceId);
        if (dataSource == null)
            return null;

        dataSource.updateFrom(data);
        return repository.save(dataSource);
    }

    public boolean delete(Id dataSourceId) {
        return repository.delete(dataSourceId);
    }

    public DataSourceWithConfiguration findDataSourceWithConfiguration(Id dataSourceId) {
        var dataSource = find(dataSourceId);
        var configuration = new DataSourceConfiguration(wrapperService.getControlWrapper(dataSource).getPathWrapper());

        return new DataSourceWithConfiguration(dataSource, configuration);
    }

    public List<DataSourceWithConfiguration> findAllDataSourcesWithConfiguration() {
        return findAll().stream().map(this::getDataSourceConfiguration).toList();
    }

    public DataSourceWithConfiguration getDataSourceConfiguration(DataSourceEntity dataSource) {
        final var configuration = new DataSourceConfiguration(wrapperService.getControlWrapper(dataSource).getPathWrapper());

        return new DataSourceWithConfiguration(dataSource, configuration);
    }
}

