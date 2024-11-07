package cz.matfyz.wrappermongodb.inference;

import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.Model;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Type;
import java.util.Collections;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class MapMongoDocument {

    private MapMongoDocument() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(MapMongoDocument.class);

    public static RecordSchemaDescription process(Document t) {
        RecordSchemaDescription result = new RecordSchemaDescription();

        result.setName(RecordSchemaDescription.ROOT_SYMBOL);
        result.setUnique(Char.FALSE);
        result.setShareTotal(1);
        result.setShareFirst(1);
        result.setId(Char.FALSE);
        result.setTypes(Type.MAP);
        result.setModels(Model.DOC);

        ObjectArrayList/*List*/<RecordSchemaDescription> children = new ObjectArrayList/*ArrayList*/<>(t.size());

        t.forEach((key, value) -> children.add(MapMongoRecord.process(key, value, true, true)));

        Collections.sort(children);

        result.setChildren(children);

        return result;
    }

}
