package cz.cuni.matfyz.server.entity;

/**
 * 
 * @author jachym.bartik
 */
public class Job extends JobData
{
    public final String id;

    public Job(String id, String value)
    {
        super(value);
        this.id = id;
    }

    public Job(String id, JobData data)
    {
        super(data);
        this.id = id;
    }
}
