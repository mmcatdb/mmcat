package cz.matfyz.wrappercsv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.core.rsd.*;
import cz.matfyz.wrappercsv.MapCSVDocument;
import cz.matfyz.wrappercsv.MapCSVRecord;

import java.util.*;

public enum MapCSVDocument {
    INSTANCE;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MapCSVDocument.class);

    public RecordSchemaDescription process(Map<String, String> t) {
        RecordSchemaDescription result = new RecordSchemaDescription();

        result.setName("_");
        result.setUnique(Char.FALSE);
        result.setShare(new Share());
        result.setId(Char.FALSE);
        result.setTypes(Type.MAP);
        result.setModels(Model.DOC);

        List<RecordSchemaDescription> children = new ArrayList<>();

        t.forEach((key, value) -> children.add(MapCSVRecord.INSTANCE.process(key, value, true, true)));

        Collections.sort(children);

        result.setChildren(children);

        return result;
    }
    

}
