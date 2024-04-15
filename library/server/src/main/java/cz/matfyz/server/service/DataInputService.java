package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datainput.DataInputEntity;
import cz.matfyz.server.entity.datainput.DataInputConfiguration;
import cz.matfyz.server.entity.datainput.DataInputInit;
import cz.matfyz.server.entity.datainput.DataInputUpdate;
import cz.matfyz.server.entity.datainput.DataInputWithConfiguration;
import cz.matfyz.server.repository.DataInputRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author jachym.bartik
 */
@Service
public class DataInputService {

    @Autowired
    private DataInputRepository repository;

    @Autowired
    private WrapperService wrapperService;

    public DataInputEntity find(Id dataInputId) {
        return repository.find(dataInputId);
    }

    public List<DataInputEntity> findAll() {
        return repository.findAll();
    }

    public List<DataInputEntity> findAllInCategory(Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    public DataInputEntity createNew(DataInputInit data) {
        var dataInput = new DataInputEntity(null, data);
        return repository.save(dataInput);
    }

    public DataInputEntity update(Id dataInputId, DataInputUpdate data) {
        DataInputEntity dataInput = repository.find(dataInputId);
        if (dataInput == null)
            return null;

        dataInput.updateFrom(data);
        return repository.save(dataInput);
    }

    public boolean delete(Id dataInputId) {
        return repository.delete(dataInputId);
    }

    public DataInputWithConfiguration findDataInputWithConfiguration(Id dataInputId) {
        var dataInput = find(dataInputId);
        var configuration = new DataInputConfiguration(wrapperService.getControlWrapper(dataInput).getPathWrapper());

        return new DataInputWithConfiguration(dataInput, configuration);
    }

    public List<DataInputWithConfiguration> findAllDataInputsWithConfiguration() {
        return findAll().stream().map(this::getDataInputConfiguration).toList();
    }

    public DataInputWithConfiguration getDataInputConfiguration(DataInputEntity dataInput) {
        final var configuration = new DataInputConfiguration(wrapperService.getControlWrapper(dataInput).getPathWrapper());

        return new DataInputWithConfiguration(dataInput, configuration);
    }
}

