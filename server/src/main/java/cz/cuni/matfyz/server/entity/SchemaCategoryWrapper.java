package cz.cuni.matfyz.server.entity;

import cz.cuni.matfyz.core.schema.SchemaCategory;

import java.util.Collection;
import org.json.JSONObject;

/**
 * 
 * @author jachym.bartik
 */
public class SchemaCategoryWrapper extends SchemaCategoryInfo
{
    public final SchemaObjectWrapper[] objects;
    public final SchemaMorphismWrapper[] morphisms;

    public SchemaCategoryWrapper(SchemaCategoryInfo info, Collection<SchemaObjectWrapper> objects, Collection<SchemaMorphismWrapper> morphisms)
    {
        super(info.id, info.jsonValue);
        this.objects = objects.toArray(new SchemaObjectWrapper[0]);
        this.morphisms = morphisms.toArray(new SchemaMorphismWrapper[0]);
    }
}
