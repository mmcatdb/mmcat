package cz.cuni.matfyz.server.entity;

/**
 * 
 * @author jachym.bartik
 */
public class Job
{
    public final String id;
    public String value;

    public Job(String id, String value)
    {
        this.id = id;
        this.value = value;
    }
}
