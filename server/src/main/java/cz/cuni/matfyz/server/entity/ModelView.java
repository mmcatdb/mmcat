package cz.cuni.matfyz.server.entity;

/**
 * 
 * @author jachym.bartik
 */
public class ModelView {

    public int jobId;
    public String jobName;

    public ModelView(int jobId, String jobName) {
        this.jobId = jobId;
        this.jobName = jobName;
    }

    public ModelView(Model model) {
        this(model.jobId, model.jobName);
    }

}
