package cz.cuni.matfyz.server.entity;

/**
 * 
 * @author jachym.bartik
 */
public class DatabaseToDatabaseTask extends Task
{
    public String sourceDatabaseId;
    public String targetDatabaseId;
    public String accessPathJson;
}