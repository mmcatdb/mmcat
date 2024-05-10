package cz.matfyz.wrapperjson.inference;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.core.rsd.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.*;

public abstract class MapJsonDocument {

    private MapJsonDocument() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(MapJsonDocument.class);

    public static RecordSchemaDescription process(Document t) {
        RecordSchemaDescription result = new RecordSchemaDescription();

        result.setName("_");
        result.setUnique(Char.FALSE);
       // result.setShare(new Share());
        result.setId(Char.FALSE);
        result.setTypes(Type.MAP);
        result.setModels(Model.DOC);

        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>();
        //List<RecordSchemaDescription> children = new ArrayList<>();

        t.forEach((key, value) -> children.add(MapJsonRecord.process(key, value, true, true)));

        Collections.sort(children);

        result.setChildren(children);

        return result;
    }

}
