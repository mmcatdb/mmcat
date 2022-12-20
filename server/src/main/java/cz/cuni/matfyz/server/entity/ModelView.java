package cz.cuni.matfyz.server.entity;

/**
 * @author jachym.bartik
 */
public class ModelView {

    public final Id jobId;
    public final String jobLabel;

    public ModelView(Id jobId, String jobLabel) {
        this.jobId = jobId;
        this.jobLabel = jobLabel;
    }

    public ModelView(Model model) {
        this(model.jobId(), model.jobLabel());
    }

}
