package cz.matfyz.tests.example.querying;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.mapping.AccessPathBuilder;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class PostgreSQL {

    private PostgreSQL() {}

    public static final List<Datasource> datasources = Arrays.stream(Schema.kindLabels).map(label -> new Datasource(DatasourceType.postgresql, "postgresql-" + label)).toList();

    public static List<TestMapping> mappings(SchemaCategory schema) {
        final List<TestMapping> output = new ArrayList<>();
        for (int i = 0; i < Schema.kindLabels.length; i++)
            output.add(createMapping(schema, i));

        return output;
    }

    private static final TestMapping createMapping(SchemaCategory schema, int index) {
        final String label = Schema.kindLabels[index];

        return new TestMapping(
            datasources.get(index),
            schema,
            Schema.kind.get(index),
            label,
            b -> createAccessPath(b, index)
        );
    }

    private static ComplexProperty createAccessPath(AccessPathBuilder b, int index) {
        final var id = b.simple("id", Schema.kind_id.get(index));
        final var value = b.simple("value", Schema.kind_value.get(index));

        if (index == Schema.kindLabels.length - 1)
            return b.root(id, value);

        return b.root(
            id,
            value,
            b.simple(Schema.kindLabels[index + 1] + "_id", Schema.kind_nextId.get(index))
        );
    }

}
