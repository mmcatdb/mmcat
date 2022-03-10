package cz.cuni.matfyz.server.entity;

/**
 * 
 * @author jachym.bartik
 */
public class Job extends JobData
{
    public final int id;

    public Job(int id, String value)
    {
        super(value);
        this.id = id;
    }

    public Job(int id, JobData data)
    {
        super(data);
        this.id = id;
    }
}
