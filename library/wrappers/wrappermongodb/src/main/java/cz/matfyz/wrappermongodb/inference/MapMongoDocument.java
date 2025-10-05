package cz.matfyz.wrappermongodb.inference;

import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.Model;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Type;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.spark.sql.Row;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class MapMongoDocument {

    private MapMongoDocument() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(MapMongoDocument.class);

    public static RecordSchemaDescription process(Row row) {
        RecordSchemaDescription result = new RecordSchemaDescription();

        result.setName(RecordSchemaDescription.ROOT_SYMBOL);
        result.setUnique(Char.FALSE);
        result.setShareTotal(1);
        result.setShareFirst(1);
        result.setId(Char.FALSE);
        result.setTypes(Type.MAP);
        result.setModels(Model.DOC);

        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>(row.size());

        for (final String key : row.schema().fieldNames()) {
            // TODO this might need to be checked ... not sure whether Row behaves the same ways as the good old bson Document.
            final var value = row.getAs(key);
            children.add(MapMongoRecord.process(key, value, true, true));
        }

        Collections.sort(children);

        result.setChildren(children);

        return result;
    }

}
