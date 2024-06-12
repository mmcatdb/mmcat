package cz.matfyz.wrappercsv.inference;

import cz.matfyz.core.rsd.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.*;

public abstract class MapCsvDocument {

    private MapCsvDocument() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(MapCsvDocument.class);

    public static RecordSchemaDescription process(Map<String, String> t) {
        RecordSchemaDescription result = new RecordSchemaDescription();

        result.setName("_");
        result.setUnique(Char.FALSE);
        //result.setShare(new Share());
        result.setId(Char.FALSE);
        result.setTypes(Type.MAP);
        result.setModels(Model.DOC);

        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>();

        t.forEach((key, value) -> children.add(MapCsvRecord.process(key, value, true, true)));

        result.setChildren(children);

        return result;
    }

}
