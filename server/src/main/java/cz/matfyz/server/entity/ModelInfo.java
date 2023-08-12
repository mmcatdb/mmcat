package cz.matfyz.server.entity;

/**
 * @author jachym.bartik
 */
public class ModelInfo {

    public final Id jobId;
    public final String jobLabel;

    public ModelInfo(Id jobId, String jobLabel) {
        this.jobId = jobId;
        this.jobLabel = jobLabel;
    }

    public ModelInfo(Model model) {
        this(model.jobId(), model.jobLabel());
    }

}
