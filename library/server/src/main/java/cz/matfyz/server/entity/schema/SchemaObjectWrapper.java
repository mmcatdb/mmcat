package cz.matfyz.server.entity.schema;

import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.ObjectIds;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SignatureId;
import cz.matfyz.server.builder.SchemaCategoryContext;
import cz.matfyz.server.utils.Position;

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
        ObjectIds ids,
        SignatureId superId,
        String iri,
        String pimIri
    ) {

        public static Data fromSchemaObject(SchemaObject object) {
            return new Data(
                object.label(),
                object.ids(),
                object.superId(),
                object.iri,
                object.pimIri
            );
        }

        public SchemaObject toSchemaObject(Key key, SchemaCategoryContext context) {
            final var object = new SchemaObject(key, label, ids, superId, iri, pimIri);
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
