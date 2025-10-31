package cz.matfyz.server.entity.datasource;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DatasourceEntity extends Entity {

    public static final String PASSWORD_FIELD_NAME = "password";

    public String label;
    public final DatasourceType type;
    public ObjectNode settings;

    private DatasourceEntity(Id id, String label, DatasourceType type, ObjectNode settings) {
        super(id);
        this.label = label;
        this.type = type;
        this.settings = settings;
    }

    public static DatasourceEntity createNew(DatasourceInit init) {
        return new DatasourceEntity(
            Id.createNew(),
            init.label(),
            init.type(),
            init.settings()
        );
    }

    /**
     * Returns a sanitized version of the settings, where all sensitive fields (e.g., password) are removed.
     */
    public ObjectNode getSanitizedSettings() {
        final ObjectNode sanitized = settings.deepCopy();
        sanitized.remove(PASSWORD_FIELD_NAME);

        return sanitized;
    }

    /** Returns the same identifier as the corresponding {@link Datasource}. */
    public String stringIdentifier() { return id().toString(); }

    public void updateFrom(DatasourceUpdate data) {
        if (data.label() != null)
            this.label = data.label();

        if (data.settings() != null)
            this.settings = data.settings();
    }

    public Datasource toDatasource() {
        return new Datasource(type, stringIdentifier());
    }

    public record JsonValue(
        String label,
        DatasourceType type,
        ObjectNode settings
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static DatasourceEntity fromJsonValue(Id id, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        return new DatasourceEntity(
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

    public boolean isEqualToInit(DatasourceInit init) {
        return label.equals(init.label()) && type.equals(init.type()) && settings.equals(init.settings());
    }

}
