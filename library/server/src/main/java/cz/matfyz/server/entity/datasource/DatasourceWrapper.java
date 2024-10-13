package cz.matfyz.server.entity.datasource;

import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DatasourceWrapper extends Entity {

    public static final String PASSWORD_FIELD_NAME = "password";

    public String label;
    public final DatasourceType type;
    public ObjectNode settings;

    private DatasourceWrapper(Id id, String label, DatasourceType type, ObjectNode settings) {
        super(id);
        this.label = label;
        this.type = type;
        this.settings = settings;
    }

    public static DatasourceWrapper createNew(DatasourceInit init) {
        return new DatasourceWrapper(
            Id.createNew(),
            init.label(),
            init.type(),
            init.settings()
        );
    }

    public void hidePassword() {
        this.settings.remove(PASSWORD_FIELD_NAME);
    }

    public void updateFrom(DatasourceUpdate data) {
        if (data.label() != null)
            this.label = data.label();

        if (data.settings() != null)
            this.settings = data.settings();
    }

    public record JsonValue(
        String label,
        DatasourceType type,
        ObjectNode settings
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static DatasourceWrapper fromJsonValue(Id id, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        return new DatasourceWrapper(
            id,
            json.label(),
            json.type(),
            json.settings()
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            label,
            type,
            settings
        ));
    }

}
