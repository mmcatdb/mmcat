package cz.cuni.matfyz.server.entity;

/**
 * @author jachym.bartik
 */
public class ModelView {

    public final int jobId;
    public final String jobLabel;

    public ModelView(int jobId, String jobLabel) {
        this.jobId = jobId;
        this.jobLabel = jobLabel;
    }

    public ModelView(Model model) {
        this(model.jobId(), model.jobLabel());
    }

}
