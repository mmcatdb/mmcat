package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.datasource.DataSource;
import cz.cuni.matfyz.server.entity.datasource.DataSourceInit;
import cz.cuni.matfyz.server.entity.datasource.DataSourceUpdate;
import cz.cuni.matfyz.server.repository.DataSourceRepository;

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

    public DataSource find(Id dataSourceId) {
        return repository.find(dataSourceId);
    }
    
    public List<DataSource> findAll() {
        return repository.findAll();
    }

    public List<DataSource> findAllInCategory(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public DataSource createNew(DataSourceInit init) {
        var dataSource = new DataSource.Builder().fromInit(init);
        return repository.save(dataSource);
    }

    public DataSource update(Id dataSourceId, DataSourceUpdate update) {
        DataSource dataSource = repository.find(dataSourceId);
        if (dataSource == null)
            return null;
        
        dataSource.updateFrom(update);
        return repository.save(dataSource);
    }

    public boolean delete(Id dataSourceId) {
        return repository.delete(dataSourceId);
    }

}
