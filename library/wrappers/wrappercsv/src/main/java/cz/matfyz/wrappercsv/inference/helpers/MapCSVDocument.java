package cz.matfyz.wrappercsv.inference.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.core.rsd.*;
import cz.matfyz.wrappercsv.inference.helpers.MapCSVDocument;

import it.unimi.dsi.fastutil.objects.ObjectArrayList; 

import java.util.*;

public enum MapCSVDocument {
    INSTANCE;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MapCSVDocument.class);

    public RecordSchemaDescription process(Map<String, String> t) {
        RecordSchemaDescription result = new RecordSchemaDescription();

        result.setName("_");
        result.setUnique(Char.FALSE);
        //result.setShare(new Share());
        result.setId(Char.FALSE);
        result.setTypes(Type.MAP);
        result.setModels(Model.DOC);

        //List<RecordSchemaDescription> children = new ArrayList<>();

        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>();

        t.forEach((key, value) -> children.add(MapCSVRecord.INSTANCE.process(key, value, true, true)));
        
        //Collections.sort(children);

        result.setChildren(children);

        return result;
    }
    

}
