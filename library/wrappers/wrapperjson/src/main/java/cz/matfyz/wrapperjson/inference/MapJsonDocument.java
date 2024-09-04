package cz.matfyz.wrapperjson.inference;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.core.rsd.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.*;

/**
 * An abstract class responsible for processing a JSON document represented by a BSON {@link Document}
 * and converting it into a {@link RecordSchemaDescription} structure.
 */
public abstract class MapJsonDocument {

    private MapJsonDocument() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(MapJsonDocument.class);

    /**
     * Processes a given BSON {@link Document} and produces a {@link RecordSchemaDescription}
     * representing the schema of the JSON document.
     *
     * @param t the BSON {@link Document} to process.
     * @return a {@link RecordSchemaDescription} representing the structure and schema
     *         inferred from the input document.
     */
    public static RecordSchemaDescription process(Document t) {
        RecordSchemaDescription result = new RecordSchemaDescription();

        result.setName("_");
        result.setUnique(Char.FALSE);
        // result.setShare(new Share());
        result.setId(Char.FALSE);
        result.setTypes(Type.MAP);
        result.setModels(Model.DOC);

        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>();

        t.forEach((key, value) -> children.add(MapJsonRecord.process(key, value, true, true)));

        Collections.sort(children);

        result.setChildren(children);

        return result;
    }

}
