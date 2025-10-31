package cz.matfyz.tests.example.querying;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;

import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;

public class Schema {

    public static final String schemaLabel = "Querying Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    // Deeply nested kinds

    public static final String[] kindLabels = new String[] { "a", "b", "c", "d", "e", "f" };

    public static final List<BuilderObjex> kind = new ArrayList<>();
    public static final List<BuilderObjex> id = new ArrayList<>();
    public static final List<BuilderObjex> value = new ArrayList<>();

    public static final List<BuilderMorphism> kind_id = new ArrayList<>();
    public static final List<BuilderMorphism> kind_value = new ArrayList<>();
    public static final List<BuilderMorphism> kind_nextKind = new ArrayList<>();
    public static final List<Signature> kind_nextId = new ArrayList<>();

    static {
        // Keys
        for (int i = 0; i < kindLabels.length; i++) {
            final String label = kindLabels[i];
            int key = (i + 1) * 10;
            kind.add(builder.objex(label, key++));
            id.add(builder.objex(label + "-id", key++));
            value.add(builder.objex(label + "-value", key++));
        }

        // Signatures
        for (int i = 0; i < kindLabels.length; i++) {
            int signature = (i + 1) * 10;
            final BuilderObjex _kind = kind.get(i);
            final BuilderMorphism _kind_id = builder.morphism(_kind, id.get(i), signature++);
            kind_id.add(_kind_id);
            kind_value.add(builder.morphism(_kind, value.get(i), signature++));

            // Ids
            builder.ids(_kind, _kind_id);

            if (i != kindLabels.length - 1)
                kind_nextKind.add(builder.morphism(_kind, kind.get(i + 1), signature++));
        }

        for (int i = 0; i < kindLabels.length - 1; i++)
            kind_nextId.add(builder.concatenate(kind_nextKind.get(i), kind_id.get(i + 1)));
    }

    // A separate kind for joins
    //
    // x5 <- z3 <- x6
    //        ^
    //        |
    // x0 <- z0 -> z1 -> z2
    //        |     |
    //        v     v
    // x2 -> x1    x4
    //        |
    //        v
    //       x3
    //
    // The y objexes mirror the x objexes. The z0 objex is the root, z2 is its id.

    public static final BuilderObjex z0 = builder.objex("z0", 100);
    public static final BuilderObjex z1 = builder.objex("z1", 101);
    public static final BuilderObjex z2 = builder.objex("z2", 102);
    public static final BuilderObjex z3 = builder.objex("z3", 103);

    public static final BuilderObjex x0 = builder.objex("x0", 110);
    public static final BuilderObjex x1 = builder.objex("x1", 111);
    public static final BuilderObjex x2 = builder.objex("x2", 112);
    public static final BuilderObjex x3 = builder.objex("x3", 113);
    public static final BuilderObjex x4 = builder.objex("x4", 114);
    public static final BuilderObjex x5 = builder.objex("x5", 115);
    public static final BuilderObjex x6 = builder.objex("x6", 116);

    public static final BuilderObjex y0 = builder.objex("y0", 120);
    public static final BuilderObjex y1 = builder.objex("y1", 121);
    public static final BuilderObjex y2 = builder.objex("y2", 122);
    public static final BuilderObjex y3 = builder.objex("y3", 123);
    public static final BuilderObjex y4 = builder.objex("y4", 124);
    public static final BuilderObjex y5 = builder.objex("y5", 125);
    public static final BuilderObjex y6 = builder.objex("y6", 126);

    public static final BuilderMorphism z0_z1 = builder.morphism(z0, z1, 101);
    public static final BuilderMorphism z1_z2 = builder.morphism(z1, z2, 102);
    public static final BuilderMorphism z0_z3 = builder.morphism(z0, z3, 103);
    public static final Signature       z0_z2 = builder.concatenate(z0_z1, z1_z2);

    public static final BuilderMorphism z0_x0 = builder.morphism(z0, x0, 110);
    public static final BuilderMorphism z0_x1 = builder.morphism(z0, x1, 111);
    public static final Signature       x1_x2 = builder.morphism(x2, x1, 112).dual();
    public static final BuilderMorphism x1_x3 = builder.morphism(x1, x3, 113);
    public static final BuilderMorphism z1_x4 = builder.morphism(z1, x4, 114);
    public static final BuilderMorphism z3_x5 = builder.morphism(z3, x5, 115);
    public static final Signature       z3_x6 = builder.morphism(x6, z3, 116).dual();

    public static final BuilderMorphism z0_y0 = builder.morphism(z0, y0, 120);
    public static final BuilderMorphism z0_y1 = builder.morphism(z0, y1, 121);
    public static final Signature       y1_y2 = builder.morphism(y2, y1, 122).dual();
    public static final BuilderMorphism y1_y3 = builder.morphism(y1, y3, 123);
    public static final BuilderMorphism z1_y4 = builder.morphism(z1, y4, 124);
    public static final BuilderMorphism z3_y5 = builder.morphism(z3, y5, 125);
    public static final Signature       z3_y6 = builder.morphism(y6, z3, 126).dual();

    static {
        builder.ids(z0, z0_z2);
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
