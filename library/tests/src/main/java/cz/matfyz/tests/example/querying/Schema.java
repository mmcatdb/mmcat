package cz.matfyz.tests.example.querying;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObject;

import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;

public class Schema {

    public static final String[] kindLabels = new String[] {
        "a", "b", "c", "d", "e", "f"
    };

    public static final String schemaLabel = "Querying Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    public static final List<BuilderObject> kind = new ArrayList<>();
    public static final List<BuilderObject> id = new ArrayList<>();
    public static final List<BuilderObject> value = new ArrayList<>();

    public static final List<BuilderMorphism> kind_id = new ArrayList<>();
    public static final List<BuilderMorphism> kind_value = new ArrayList<>();
    public static final List<BuilderMorphism> kind_nextKind = new ArrayList<>();
    public static final List<BuilderMorphism> kind_nextId = new ArrayList<>();

    static {
        // Keys
        for (final String label : kindLabels) {
            kind.add(builder.object(label));
            id.add(builder.object(label + "-id"));
            value.add(builder.object(label + "-value"));
        }

        // Signatures
        for (int i = 0; i < kindLabels.length; i++) {
            final BuilderObject _kind = kind.get(i);
            final BuilderMorphism _kind_id = builder.morphism(_kind, id.get(i));
            kind_id.add(_kind_id);
            kind_value.add(builder.morphism(_kind, value.get(i)));

            // Ids
            builder.ids(_kind, _kind_id);

            if (i != kindLabels.length - 1)
                kind_nextKind.add(builder.morphism(_kind, kind.get(i + 1)));
        }

        for (int i = 0; i < kindLabels.length - 1; i++)
            kind_nextId.add(builder.composite(kind_nextKind.get(i), kind_id.get(i + 1)));
    }

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchema() {
        return builder.build();
    }

    public static MetadataCategory newMetadata(SchemaCategory schema) {
        return builder.buildMetadata(schema);
    }

    private Schema() {}

}
