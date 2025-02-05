package cz.matfyz.tests.example.querying;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.DatasourceProvider;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Datasources {

    public final SchemaCategory schema = Schema.newSchema();

    private static final List<DatasourceProvider> datasourceProviders = Arrays.stream(Schema.kindLabels).map(label -> new DatasourceProvider("tests", "querying-" + label)).toList();

    // PostgreSQL

    private List<TestDatasource<PostgreSQLControlWrapper>> postgreSQLs;

    public List<TestDatasource<PostgreSQLControlWrapper>> postgreSQLs() {
        if (postgreSQLs == null)
            postgreSQLs = createNewPostgreSQLs();

        return postgreSQLs;
    }

    public List<TestDatasource<PostgreSQLControlWrapper>> createNewPostgreSQLs() {
        final List<TestDatasource<PostgreSQLControlWrapper>> output = new ArrayList<>();
        final List<TestMapping> mappings = PostgreSQL.mappings(schema);

        for (int i = 0; i < Schema.kindLabels.length; i++) {
            final var provider = datasourceProviders.get(i);
            final var testDatasource = provider.createPostgreSQL(PostgreSQL.datasources.get(i).identifier, schema, "setupPostgresqlQuerying/" + Schema.kindLabels[i] + ".sql");
            testDatasource.addMapping(mappings.get(i));
            output.add(testDatasource);
        }

        return output;
    }

}
