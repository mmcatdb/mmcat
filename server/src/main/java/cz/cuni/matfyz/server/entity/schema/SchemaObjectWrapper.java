package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.ObjectIds;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.schema.SignatureId;
import cz.cuni.matfyz.server.builder.SchemaCategoryContext;
import cz.cuni.matfyz.server.utils.Position;

/**
 * @author jachym.bartik
 */
public record SchemaObjectWrapper(
    Key key,
    Position position,
    String label,
    SignatureId superId,
    ObjectIds ids,
    String iri,
    String pimIri
) {

    public static SchemaObjectWrapper fromSchemaObject(SchemaObject object, SchemaCategoryContext context) {
        return new SchemaObjectWrapper(
            object.key(),
            context.getPosition(object.key()),
            object.label(),
            object.superId(),
            object.ids(),
            object.iri,
            object.pimIri
        );
    }

    public SchemaObject toSchemaObject(SchemaCategoryContext context) {
        context.setPosition(key, position);
        final var object = new SchemaObject(key, label, superId, ids);
        context.setObject(object);
        
        return object;
    }

}
