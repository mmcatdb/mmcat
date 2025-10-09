package cz.matfyz.tests.example.common;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDatasource<TWrapper extends AbstractControlWrapper> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDatasource.class);

    private final DatasourceType type;
    private final String identifier;

    public final TWrapper wrapper;
    public final List<Mapping> mappings = new ArrayList<>();
    public final SchemaCategory schema;
    private final @Nullable String setupFilename;

    public TestDatasource(DatasourceType type, String identifier, TWrapper wrapper, SchemaCategory schema, @Nullable String setupFilename) {
        this.type = type;
        this.identifier = identifier;
        this.wrapper = wrapper;
        this.schema = schema;
        this.setupFilename = setupFilename;
    }

    public TestDatasource<TWrapper> addMapping(TestMapping testMapping) {
        mappings.add(testMapping.mapping());

        return this;
    }

    public void setup() {
        final var filePath = getFilePath();
        if (filePath != null)
            wrapper.execute(filePath);
    }

    private @Nullable Path getFilePath() {
        if (setupFilename == null)
            return null;

        try {
            final var url = ClassLoader.getSystemResource(setupFilename);
            return Paths.get(url.toURI()).toAbsolutePath();
        }
        catch (URISyntaxException e) {
            LOGGER.error("Datasource setup error: ", e);
            throw new RuntimeException(e);
        }
    }

    private @Nullable Datasource datasource;

    public Datasource datasource() {
        if (datasource == null)
            datasource = new Datasource(type, identifier);

        return datasource;
    }

}
