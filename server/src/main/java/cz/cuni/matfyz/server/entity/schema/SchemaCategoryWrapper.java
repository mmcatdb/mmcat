package cz.cuni.matfyz.server.entity.schema;

import java.util.Collection;

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
