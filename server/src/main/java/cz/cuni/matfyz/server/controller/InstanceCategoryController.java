package cz.cuni.matfyz.server.controller;

import cz.cuni.matfyz.core.instance.ActiveDomainRow;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.server.service.InstanceCategoryService;
import cz.cuni.matfyz.server.view.InstanceObjectView;
import cz.cuni.matfyz.server.view.SignatureValueTuple;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 
 * @author jachym.bartik
 */
@RestController
public class InstanceCategoryController {

    @Autowired
    private InstanceCategoryService service;

    @GetMapping("/instances")
    public List<String> getAllInstances(HttpSession session) {
        var instances = service.findAll(session);

        return instances.stream().map(instance -> instance.toString()).toList();
    }

    @GetMapping("/instances/default/object/{objectKey}")
    public String getInstanceObject(HttpSession session, @PathVariable Integer objectKey) {
        var key = new Key(objectKey);

        var object = service.findObject(session, key);
        /*
        System.out.println(object);
        if (object != null) {
            var view = new InstanceObjectView();
            view.key = object.key();

            var rows = new ArrayList<List<SignatureValueTuple>>();
            for (var innerMap : object.activeDomain().values()) {
                for (ActiveDomainRow row : innerMap.values()) {
                    var map = row.idWithValues().map();

                    var newRow = new ArrayList<SignatureValueTuple>();
                    
                    map.keySet().stream().forEach(signature -> {
                        var tuple = new SignatureValueTuple();
                        tuple.signature = signature;
                        tuple.value = map.get(signature);
                        newRow.add(tuple);
                    });

                    rows.add(newRow);
                }
            }

            view.rows = rows;

            System.out.println(view);

            return view;
        }
        */

        if (object != null)
            return object.toJSON().toString();
            
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

}
