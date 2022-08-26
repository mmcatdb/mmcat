package cz.cuni.matfyz.server.entity;

/**
 * @author jachym.bartik
 */
public class ModelView {

    public final int jobId;
    public final String jobName;

    public ModelView(int jobId, String jobName) {
        this.jobId = jobId;
        this.jobName = jobName;
    }

    public ModelView(Model model) {
        this(model.jobId(), model.jobName());
    }

}
