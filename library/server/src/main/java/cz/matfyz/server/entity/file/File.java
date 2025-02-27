package cz.matfyz.server.entity.file;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import java.util.Date;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a generic file object that can store various types of data.
 * However, primarily intended to hold the outputs of jobs.
 */
public class File extends Entity {

    public enum FileType {
        DML, DATA_FILE;
    }

    public @Nullable Id jobId;
    public @Nullable Id datasourceId;
    public final String name;
    public final String filename;
    public final Date createdAt;
    public final FileType type;

    private File(Id id, @Nullable Id jobId, @Nullable Id datasourceId, String name, String filename, Date createdAt, FileType type) {
        super(id);
        this.jobId = jobId;
        this.datasourceId = datasourceId;
        this.name = name;
        this.filename = filename;
        this.createdAt = createdAt;
        this.type = type;
    }

    public static File createnew(@Nullable Id jobId, @Nullable Id datasourceId, String name, FileType type, String contents) {
        return new File(
            Id.createNew(),
            jobId,
            datasourceId,
            name,
            createFilename(name),
            new Date(),
            type
        );
    }

    private static String createFilename(String name) {

        return name;
    }


}
