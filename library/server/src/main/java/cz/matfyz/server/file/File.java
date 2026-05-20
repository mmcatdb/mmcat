package cz.matfyz.server.file;

import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.server.utils.Configuration.UploadsProperties;
import cz.matfyz.server.utils.entity.Entity;
import cz.matfyz.server.utils.entity.Id;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Represents a generic file object that can store various fileTypes of data.
 * However, primarily intended to hold the outputs of CtM jobs.
 */
public class File extends Entity {

    public Id jobId;
    public Id datasourceId;
    public String label;
    public String description;
    public final DatasourceType fileType;
    public final Date createdAt;
    public final List<Date> executedAt;

    String filename() {
        // The file is stored with the extension so that it can be easily worked with locally.
        return id().toString() + "." + fileExtension();
    }

    Path path(UploadsProperties uploads) {
        return Paths.get(uploads.directory(), filename());
    }


    private File(Id id, Id jobId, Id datasourceId, String label, String description, DatasourceType fileType, Date createdAt, List<Date> executedAt) {
        super(id);
        this.jobId = jobId;
        this.datasourceId = datasourceId;
        this.label = label;
        this.description = description;
        this.fileType = fileType;
        this.createdAt = createdAt;
        this.executedAt = executedAt;
    }

    static File createNew(Id jobId, Id datasourceId, boolean isExecuted, DatasourceType datasourceType) {
        final var now = new Date();
        final var executedAt = new ArrayList<Date>();
        if (isExecuted)
            executedAt.add(now);

        return new File(
            Id.createNew(),
            jobId,
            datasourceId,
            getInitialLabel(datasourceType),
            "",
            datasourceType,
            now,
            executedAt
        );
    }

    private static String getInitialLabel(DatasourceType fileType) {
        return switch (fileType) {
            case json, jsonld, csv -> capitalize(fileType.name()) + " File";
            case mongodb, postgresql, neo4j -> capitalize(fileType.name()) + " DML Commands";
            default -> "Unknown file type";
        };
    }

    private static String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private String fileExtension() {
        return switch (fileType) {
            case json -> "json";
            case csv -> "csv";
            default -> "txt"; // DML stored as txt.
        };
    }

    public void addExecutionDate(Date executionDate) {
        this.executedAt.add(executionDate);
    }

    private record JsonValue(
        String label,
        String description,
        DatasourceType fileType,
        Date createdAt,
        List<Date> executedAt
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static File fromJsonValue(Id id, Id jobId, Id datasourceId, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        return new File(
            id,
            jobId,
            datasourceId,
            json.label,
            json.description,
            json.fileType,
            json.createdAt,
            json.executedAt
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(label, description, fileType, createdAt, executedAt));
    }

}
