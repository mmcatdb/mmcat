package cz.cuni.matfyz.server.entity;

/**
 * 
 * @author jachym.bartik
 */
public class Job extends Entity {

    public final int mappingId;
    public final String value;

    public Job(Integer id, int mappingId, String value) {
        super(id);
        this.mappingId = mappingId;
        this.value = value;
    }

    public Job(Integer id, JobData data) {
        super(id);
        this.mappingId = data.mappingId;
        this.value = data.value;
    }
    
}
