package cz.matfyz.wrapperjson.inference.helpers;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.core.rsd.*;

import java.util.*;

public enum MapJSONDocument {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(MapJSONDocument.class);

    public RecordSchemaDescription process(Document t) {
        RecordSchemaDescription result = new RecordSchemaDescription();

        result.setName("_");
        result.setUnique(Char.FALSE);
        result.setShare(new Share());
        result.setId(Char.FALSE);
        result.setTypes(Type.MAP);
        result.setModels(Model.DOC);

        List<RecordSchemaDescription> children = new ArrayList<>();

        t.forEach((key, value) -> children.add(MapJSONRecord.INSTANCE.process(key, value, true, true)));

        Collections.sort(children);

        result.setChildren(children);

        return result;
    }

}
