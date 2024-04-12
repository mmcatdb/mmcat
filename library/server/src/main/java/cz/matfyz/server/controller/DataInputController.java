package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datainput.DataInputEntity;
import cz.matfyz.server.entity.datainput.DataInputInfo;
import cz.matfyz.server.entity.datainput.DataInputInit;
import cz.matfyz.server.entity.datainput.DataInputUpdate;
import cz.matfyz.server.entity.datainput.DataInputWithConfiguration;
import cz.matfyz.server.service.DataInputService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author jachym.bartik
 */
@RestController
public class DataInputController {

    @Autowired
    private DataInputService service;

    @GetMapping("/data-input-infos")
    public List<DataInputWithConfiguration> getAllDataInputInfos() {
        return service.findAllDataInputsWithConfiguration();
    }

    @GetMapping("/data-inputs")
    public List<DataInputEntity> getAllDataInputs(@RequestParam Optional<Id> categoryId) {
        var dataInputs = categoryId.isPresent() ? service.findAllInCategory(categoryId.get()) : service.findAll();
        dataInputs.forEach(DataInputEntity::hidePassword);
        return dataInputs;
    }

    @GetMapping("/data-inputs/{id}")
    public DataInputEntity getDataInput(@PathVariable Id id) {
        DataInputEntity dataInput = service.find(id);
        if (dataInput == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        dataInput.hidePassword();
        return dataInput;
    }

    @PostMapping("/data-inputs")
    public DataInputInfo createDataInput(@RequestBody DataInputInit data) {
        DataInputEntity dataInput = service.createNew(data);
        if (dataInput == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        return dataInput.toInfo();
    }

    @PutMapping("/data-inputs/{id}")
    public DataInputInfo updateDataInput(@PathVariable Id id, @RequestBody DataInputUpdate update) {
        if (!update.hasPassword()) {
            var originalDataInput = service.find(id);
            update.setPasswordFrom(originalDataInput);
        }
        DataInputEntity dataInput = service.update(id, update);
        if (dataInput == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        return dataInput.toInfo();
    }

    @DeleteMapping("/data-inputs/{id}")
    public void deleteDataInput(@PathVariable Id id) {
        boolean status = service.delete(id);
        if (!status)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The data input can't be deleted. Check that there aren't any mappings that depend on it.");
    }

}

