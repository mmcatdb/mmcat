package cz.matfyz.tests.example.common;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.database.Database.DatabaseType;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDatabase<TWrapper extends AbstractControlWrapper> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDatabase.class);

    private static int lastId = 0;

    public final DatabaseType type;
    public final String id;
    public final TWrapper wrapper;
    public final List<Mapping> mappings = new ArrayList<>();
    public final SchemaCategory schema;
    private final String setupFileName;

    public TestDatabase(DatabaseType type, TWrapper wrapper, SchemaCategory schema, String setupFileName) {
        this.type = type;
        this.id = "" + lastId++;
        this.wrapper = wrapper;
        this.schema = schema;
        this.setupFileName = setupFileName;
    }

    public TestDatabase<TWrapper> addMapping(TestMapping testMapping) {
        mappings.add(testMapping.mapping());

        return this;
    }

    public void setup() {
        wrapper.execute(getFilePath());
    }

    private Path getFilePath() {
        try {
            final var url = ClassLoader.getSystemResource(setupFileName);
            return Paths.get(url.toURI()).toAbsolutePath();
        }
        catch (URISyntaxException e) {
            LOGGER.error("Database setup error: ", e);
            throw new RuntimeException(e);
        }
    }
    
}
