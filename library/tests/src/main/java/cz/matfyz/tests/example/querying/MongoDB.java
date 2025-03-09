package cz.matfyz.tests.example.querying;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class MongoDB {

    private MongoDB() {}

    public static final Datasource datasource = new Datasource(DatasourceType.mongodb, "mongodb");

    public static final String xKind = "x";
    public static final String yKind = "y";

    public static TestMapping x(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.z0,
            xKind,
            b -> b.root(
                b.simple("x0", Schema.z0_x0),
                b.complex("x1", Schema.z0_x1,
                    b.simple("x2", Schema.x1_x2),
                    b.simple("x3", Schema.x1_x3)
                ),
                b.complex("z1", Schema.z0_z1,
                    b.simple("x4", Schema.z1_x4),
                    b.simple("z2", Schema.z1_z2)
                ),
                b.complex("z3", Schema.z0_z3,
                    b.simple("x5", Schema.z3_x5),
                    b.simple("x6", Schema.z3_x6)
                )
            )
        );
    }

    public static TestMapping y(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.z0,
            yKind,
            b -> b.root(
                b.simple("y0", Schema.z0_y0),
                b.complex("y1", Schema.z0_y1,
                    b.simple("y2", Schema.y1_y2),
                    b.simple("y3", Schema.y1_y3)
                ),
                b.complex("z1", Schema.z0_z1,
                    b.simple("y4", Schema.z1_y4),
                    b.simple("z2", Schema.z1_z2)
                ),
                b.complex("z3", Schema.z0_z3,
                    b.simple("y5", Schema.z3_y5),
                    b.simple("y6", Schema.z3_y6)
                )
            )
        );
    }

}
