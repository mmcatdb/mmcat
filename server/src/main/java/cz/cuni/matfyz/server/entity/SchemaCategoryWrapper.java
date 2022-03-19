package cz.cuni.matfyz.server.entity;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.serialization.MapUniqueContext;
import cz.cuni.matfyz.core.serialization.UniqueContext;

import java.util.Collection;
import org.json.JSONObject;

/**
 * 
 * @author jachym.bartik
 */
public class SchemaCategoryWrapper extends SchemaCategoryInfo {

    public final Collection<SchemaObjectWrapper> objects;
    public final Collection<SchemaMorphismWrapper> morphisms;

    public SchemaCategoryWrapper(SchemaCategoryInfo info, Collection<SchemaObjectWrapper> objects, Collection<SchemaMorphismWrapper> morphisms) {
        super(info.id, info.jsonValue);
        this.objects = objects;
        this.morphisms = morphisms;
    }

}
