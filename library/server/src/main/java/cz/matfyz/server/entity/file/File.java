package cz.matfyz.server.entity.file;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.global.Configuration.UploadsProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a generic file object that can store various fileTypes of data.
 * However, primarily intended to hold the outputs of  CtM jobs.
 */
public class File extends Entity {

    public enum FileType {
        DML, DATA_FILE;
    }

    public @Nullable Id jobId;
    public @Nullable Id datasourceId;
    public @Nullable Id categoryId;
    public final String filename;
    public final FileType fileType;

    private File(Id id, @Nullable Id jobId, @Nullable Id datasourceId, @Nullable Id categoryId,  FileType fileType) {
        super(id);
        this.jobId = jobId;
        this.datasourceId = datasourceId;
        this.categoryId = categoryId;
        this.filename = id.toString();
        this.fileType = fileType;
    }

    public static File createnew(@Nullable Id jobId, @Nullable Id datasourceId, @Nullable Id categoryId, FileType fileType, String contents, UploadsProperties uploads) {
        Id newId = Id.createNew();

        File newFile = new File(
            newId,
            jobId,
            datasourceId,
            categoryId,
            fileType
        );

        newFile.saveToFile(contents, uploads);
        return newFile;
    }

    /**
     * Saves the file object as a plain text file (in the uploads folder).
     */
    public void saveToFile(String contents, UploadsProperties uploads) {
        try {
            Files.writeString(Paths.get(getFilePath(uploads)), contents, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + getFilePath(uploads), e);
        }
    }

    /**
     * Reads a JSON file and reconstructs the object
    */
    /*
    public File loadFromFile(String filename, UploadsProperties uploads) {
        try {
            String jsonValue = Files.readString(Paths.get(getFilePath(uploads)));
            return fromJsonValue(jsonValue);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + filename, e);
        }
    }*/

    private String getFilePath(UploadsProperties uploads) {
        return uploads.folder() + "/" + filename + ".txt";
    }

    public static File fromDatabase(Id id, Id jobId, Id datasourceId, Id categoryId, String fileTypeString) {
        return new File(
            id,
            jobId,
            datasourceId,
            categoryId,
            FileType.valueOf(fileTypeString)
        );
    }

}
