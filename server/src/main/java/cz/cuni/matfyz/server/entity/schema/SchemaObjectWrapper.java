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
    Data data,
    Metadata metadata
) {

    public static SchemaObjectWrapper fromSchemaObject(SchemaObject object, SchemaCategoryContext context) {
        final var data = Data.fromSchemaObject(object);
        final var metadata = new Metadata(context.getPosition(object.key()));

        return new SchemaObjectWrapper(object.key(), data, metadata);
    }

    public SchemaObject toSchemaObject(SchemaCategoryContext context) {
        context.setPosition(key, metadata.position);

        return data.toSchemaObject(key, context);
    }

    public record Data(
        String label,
        SignatureId superId,
        ObjectIds ids,
        String iri,
        String pimIri
    ) {

        public static Data fromSchemaObject(SchemaObject object) {
            return new Data(
                object.label(),
                object.superId(),
                object.ids(),
                object.iri,
                object.pimIri
            );
        }

        public SchemaObject toSchemaObject(Key key, SchemaCategoryContext context) {
            final var object = new SchemaObject(key, label, superId, ids, iri, pimIri);
            context.setObject(object);
            
            return object;
        }

    }

    public record Metadata(
        Position position
    ) {}

    public record MetadataUpdate(
        Key key,
        Position position
    ) {}

}
