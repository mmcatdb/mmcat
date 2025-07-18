package cz.matfyz.server.entity.file;

import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.global.Configuration.UploadsProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a generic file object that can store various fileTypes of data.
 * However, primarily intended to hold the outputs of CtM jobs.
 */
public class File extends Entity {

    private static final int PREVIEW_LIMIT = 1000;

    public @Nullable Id jobId;
    public @Nullable Id datasourceId;
    public @Nullable Id categoryId;
    public String label;
    public String description;
    public final String filename; // filename under which the file is stored = the file's id
    public final String jobLabel;
    public final DatasourceType fileType;
    public final Date createdAt;
    public @Nullable List<Date> executedAt;

    private File(Id id, @Nullable Id jobId, @Nullable Id datasourceId, @Nullable Id categoryId, String label, String description, String jobLabel, DatasourceType fileType, Date createdAt, @Nullable List<Date> executedAt) {
        super(id);
        this.jobId = jobId;
        this.datasourceId = datasourceId;
        this.categoryId = categoryId;
        this.label = label;
        this.description = description;
        this.jobLabel = jobLabel;
        this.filename = id.toString();
        this.fileType = fileType;
        this.createdAt = createdAt;
        this.executedAt = executedAt;
    }

    public static File createnew(@Nullable Id jobId, @Nullable Id datasourceId, @Nullable Id categoryId, String jobLabel, boolean executed, DatasourceType datasourceType, String contents, UploadsProperties uploads) {
        Id newId = Id.createNew();

        File newFile = new File(
            newId,
            jobId,
            datasourceId,
            categoryId,
            getInitialLabel(datasourceType),
            null,
            jobLabel,
            datasourceType,
            new Date(),
            executed ? new ArrayList<>(List.of(new Date())) : null
        );

        saveToFile(newFile, contents, uploads);
        return newFile;
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

    /**
     * Get the file path based on the file type
     */
    public static String getFilePath(File file, UploadsProperties uploads) {
        return uploads.folder() + "/" + file.filename + getFileExtension(file.fileType);
    }

    private static String getFileExtension(DatasourceType fileType) {
        return switch (fileType) {
            case json -> ".json";
            case csv -> ".csv";
            default -> ".txt"; // DML stored as .txt
        };
    }

    /**
     * Saves the file object in different formats based on file type.
     */
    private static void saveToFile(File file, String contents, UploadsProperties uploads) {
        try {
            Files.writeString(Paths.get(getFilePath(file, uploads)), contents, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + getFilePath(file, uploads), e);
        }
    }

    public void addExecutionDate(Date executionDate) {
        if (this.executedAt == null)
            this.executedAt = new ArrayList<>();
        this.executedAt.add(executionDate);
    }

    public List<String> readExecutionCommands(UploadsProperties uploads) {
        String filePath = getFilePath(this, uploads);

        try {
            String fileContents = Files.readString(Paths.get(filePath));
            fileContents = fileContents.replaceAll("\\s+", " ").trim();
            return List.of(fileContents.split("(?<=;)(?=(?:[^\"']*[\"'][^\"']*[\"'])*[^\"']*$)"));

        } catch (IOException e) {
            throw new RuntimeException("Failed to read execution file: " + filePath, e);
        }
    }

    public String readPreview(UploadsProperties uploads) {
        String filePath = getFilePath(this, uploads);

        StringBuilder preview = new StringBuilder();
        int linesRead = 0;

        try (
            var reader = Files.newBufferedReader(Paths.get(filePath))
        ) {
            String line;
            while ((line = reader.readLine()) != null && linesRead < PREVIEW_LIMIT) {
                preview.append(line).append("\n");
                linesRead++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read preview for file: " + filePath, e);
        }

        return preview.toString();
    }

    private record JsonValue(
        String label,
        String description,
        String jobLabel,
        DatasourceType fileType,
        Date createdAt,
        List<Date> executedAt
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static File fromJsonValue(Id id, Id jobId, Id datasourceId, Id categoryId, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        return new File(
            id,
            jobId,
            datasourceId,
            categoryId,
            json.label,
            json.description,
            json.jobLabel,
            json.fileType,
            json.createdAt,
            json.executedAt
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(label, description, jobLabel, fileType, createdAt, executedAt));
    }

}
