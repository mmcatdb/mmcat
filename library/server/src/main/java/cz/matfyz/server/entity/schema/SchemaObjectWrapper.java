package cz.matfyz.server.entity.schema;

import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.ObjectIds;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SignatureId;
import cz.matfyz.server.builder.MetadataContext;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author jachym.bartik
 */
public record SchemaObjectWrapper(
    Key key,
    Data data,
    Metadata metadata
) {

    public static SchemaObjectWrapper fromSchemaObject(SchemaObject object, MetadataContext context) {
        final var data = Data.fromSchemaObject(object);
        final var metadata = new Metadata(context.getPosition(object.key()));

        return new SchemaObjectWrapper(object.key(), data, metadata);
    }

    public SchemaObject toSchemaObject(@Nullable MetadataContext context) {
        if (context != null)
            context.setPosition(key, metadata.position);

        return data.toSchemaObject(key);
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

        public SchemaObject toSchemaObject(Key key) {
            return new SchemaObject(key, label, ids, superId, iri, pimIri);
        }

    }

    public record Position(
        double x,
        double y
    ) {}

    public record Metadata(
        Position position
    ) {}

    public record MetadataUpdate(
        Key key,
        Position position
    ) {}

}
