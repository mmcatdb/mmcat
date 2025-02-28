package cz.matfyz.server.entity.file;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.global.Configuration.UploadsProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a generic file object that can store various fileTypes of data.
 * However, primarily intended to hold the outputs of jobs.
 */
public class File extends Entity {

    public enum FileType {
        DML, DATA_FILE;
    }

    public @Nullable Id jobId;
    public @Nullable Id datasourceId;
    public final String label;
    public final String filename;
    public final Date createdAt;
    public final FileType fileType;
    public final String contents;

    private File(Id id, @Nullable Id jobId, @Nullable Id datasourceId, String label, String filename, Date createdAt, FileType fileType, String contents) {
        super(id);
        this.jobId = jobId;
        this.datasourceId = datasourceId;
        this.label = label;
        this.filename = filename;
        this.createdAt = createdAt;
        this.fileType = fileType;
        this.contents = contents;
    }

    public static File createnew(@Nullable Id jobId, @Nullable Id datasourceId, String label, FileType fileType, String contents, UploadsProperties uploads) {
        Id newId = Id.createNew();

        File newFile = new File(
            newId,
            jobId,
            datasourceId,
            label,
            createfilename(label, newId),
            new Date(),
            fileType,
            contents
        );

        newFile.saveToFile();
        return newFile;
    }

    private static String createfilename(String label, Id id) {
        return label + "_" + id;
    }

    private record JsonValue(
        @Nullable Id jobId,
        @Nullable Id datasourceId,
        Date createdAt,
        FileType fileType,
        String contents
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    /**
     * Saves the file object as a JSON file (in the uploads folder)
     */
    public void saveToFile() {
        try {
            Files.write(Paths.get(getFilePath()), toJsonValue().getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + getFilePath(), e);
        }
    }

    /**
     * Reads a JSON file and reconstructs the object
    */
    public File loadFromFile(String filename) {
        try {
            String jsonValue = Files.readString(Paths.get(getFilePath()));
            return fromJsonValue(jsonValue);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + filename, e);
        }
    }

    private String getFilePath() {
        return "uploads" + "/" + filename + ".json";
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            jobId,
            datasourceId,
            createdAt,
            fileType,
            contents
        ));
    }

    public static File fromJsonValue(String jsonValue) throws JsonProcessingException {
        JsonValue json = jsonValueReader.readValue(jsonValue);
        return new File(
            Id.createNew(), //TODO
            json.jobId,
            json.datasourceId,
            "", //TODO
            "", //TODO
            json.createdAt,
            json.fileType,
            json.contents
        );
    }


}
