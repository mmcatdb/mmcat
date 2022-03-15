package cz.cuni.matfyz.server.entity;

import java.io.Serializable;

/**
 * 
 * @author jachym.bartik
 */
public class JobData implements Serializable
{
    public int mappingId;
    public String value;

    //public Task task; // TODO - zde budou uložena data pro daný task (ale ne metadata, jako třeba průběh, čas startu a tak)

    public JobData(int mappingId, String value)
    {
        this.mappingId = mappingId;
        this.value = value;
    }

    public JobData(JobData data)
    {
        this.mappingId = data.mappingId;
        this.value = data.value;
    }
}
