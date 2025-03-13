package cz.matfyz.server.entity.file;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.global.Configuration.UploadsProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

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

    public enum FileType {
        DML, CSV, JSON;
    }

    public @Nullable Id jobId;
    public @Nullable Id datasourceId;
    public @Nullable Id categoryId;
    public String label;
    public @Nullable String description;
    public final String filename; // filename under which the file is stored = the file's id
    public final String jobLabel;
    public final FileType fileType;
    public final Date createdAt;

    private File(Id id, @Nullable Id jobId, @Nullable Id datasourceId, @Nullable Id categoryId, String label, @Nullable String description, String jobLabel, FileType fileType, Date createdAt) {
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
    }

    public static File createnew(@Nullable Id jobId, @Nullable Id datasourceId, @Nullable Id categoryId, String label, FileType fileType, String contents, UploadsProperties uploads) {
        Id newId = Id.createNew();

        File newFile = new File(
            newId,
            jobId,
            datasourceId,
            categoryId,
            getInitialLabel(fileType),
            null,
            label,
            fileType,
            new Date()
        );

        saveToFile(newFile, contents, uploads);
        return newFile;
    }

    private static String getInitialLabel(FileType fileType) {
        return switch (fileType) {
            case JSON -> "JSON File";
            case CSV -> "CSV File";
            default -> "DML Commands";
        };
    }

    /**
     * Get the file path based on the file type
     */
    public static String getFilePath(File file, UploadsProperties uploads) {
        return uploads.folder() + "/" + file.filename + getFileExtension(file.fileType);
    }

    private static String getFileExtension(FileType fileType) {
        return switch (fileType) {
            case JSON -> ".json";
            case CSV -> ".csv";
            case DML -> ".txt"; // DML stored as .txt
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

    public void updateLabel(String newLabel) {
        this.label = newLabel;
    }

    private record JsonValue(
        String label,
        String description,
        String jobLabel,
        FileType fileType,
        Date createdAt
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
            json.createdAt
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(label, description, jobLabel, fileType, createdAt));
    }

}
