package cz.matfyz.server.datasource;

import cz.matfyz.server.utils.entity.Id;

import java.util.List;

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

    public List<DatasourceEntity> createIfNotExists(List<DatasourceInit> inits) {
        final List<DatasourceEntity> existingDatasources = repository.findAll();

        return inits.stream()
            .map(init -> {
                final var existing = existingDatasources.stream().filter(ed -> ed.isEqualToInit(init)).findFirst();
                return existing.isPresent()
                    ? existing.get()
                    : create(init);
            }).toList();
    }

    public DatasourceEntity update(Id datasourceId, DatasourceEdit data) {
        final DatasourceEntity datasource = repository.find(datasourceId);

        datasource.updateFrom(data);
        repository.save(datasource);

        return datasource;
    }

}

