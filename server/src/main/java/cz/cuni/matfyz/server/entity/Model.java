package cz.cuni.matfyz.server.entity;

/**
 * 
 * @author jachym.bartik
 */
public class Model {

    public int jobId;
    public int schemaId;
    public String jobName;
    public String commands;

    public Model(int jobId, int schemaId, String jobName, String commands) {
        this.jobId = jobId;
        this.schemaId = schemaId;
        this.jobName = jobName;
        this.commands = commands;
    }

}
