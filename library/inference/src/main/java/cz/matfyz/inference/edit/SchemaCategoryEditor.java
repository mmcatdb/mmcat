package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.UniqueContext;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;

import org.apache.hadoop.yarn.webapp.NotFoundException;

public class SchemaCategoryEditor extends SchemaCategory.Editor {

    public final SchemaCategory schemaCategory;

    public SchemaCategoryEditor(SchemaCategory schemaCategory) {
        this.schemaCategory = schemaCategory;
    }

    public void deleteObject(Key key) {
        UniqueContext<SchemaObject, Key> objectContext = getObjectContext(schemaCategory);
        SchemaObject objectToRemove = objectContext.getUniqueObject(key);
        if (objectToRemove != null) {
            objectContext.deleteUniqueObject(objectToRemove);
        } else {
            throw new NotFoundException("SchemaObject with the provided key does not exist");
        }
    }
}
