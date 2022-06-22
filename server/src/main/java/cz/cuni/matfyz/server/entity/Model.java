package cz.cuni.matfyz.server.entity;

/**
 * 
 * @author jachym.bartik
 */
public class Model {

    public int jobId;
    public String jobName;
    public String commands;

    public Model(int jobId, String jobName, String commands) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.commands = commands;
    }

}
