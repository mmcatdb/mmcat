package cz.cuni.matfyz.server.entity.job;

import cz.cuni.matfyz.core.serialization.FromJSONLoaderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;
import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jachym.bartik
 */
public class Job extends Entity implements JSONConvertible {

    public final Id logicalModelId;
    public final Id categoryId;
    public String label;
    public Type type;
    public Status status;

    /*
    public Job(
        @JsonProperty("id") Integer id,
        @JsonProperty("logicalModelId") int logicalModelId,
        @JsonProperty("status") Status status
    ) {
        super(id);
        this.logicalModelId = logicalModelId;
        this.status = status;
    }
    */

    private Job(Id id, Id logicalModelId, Id categoryId) {
        super(id);
        this.logicalModelId = logicalModelId;
        this.categoryId = categoryId;
    }

    public enum Status {
        Default, // The job isn't created yet.
        Ready, // The job can be started now.
        Running, // The job is currently being processed.
        Finished, // The job is finished, either with a success or with an error.
        Canceled // The job was canceled while being in one of the previous states. It can never be started (again).
    }

    public enum Type {
        ModelToCategory,
        CategoryToModel
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<Job> {

        @Override
        protected JSONObject innerToJSON(Job object) throws JSONException {
            var output = new JSONObject();

            output.put("label", object.label.toString());
            output.put("type", object.type.toString());
            output.put("status", object.status.toString());

            return output;
        }

    }

    public static class Builder extends FromJSONLoaderBase<Job> {

        public Job fromJSON(Id id, Id logicalModelId, Id categoryId, String jsonValue) {
            var job = new Job(id, logicalModelId, categoryId);
            loadFromJSON(job, jsonValue);
            return job;
        }

        @Override
        protected void innerLoadFromJSON(Job job, JSONObject jsonObject) throws JSONException {
            job.label = jsonObject.getString("label");
            job.type = Type.valueOf(jsonObject.getString("type"));
            job.status = Status.valueOf(jsonObject.getString("status"));
        }

        public Job fromArguments(Id id, Id logicalModelId, Id categoryId, String label, Type type, Status status) {
            var job = new Job(id, logicalModelId, categoryId);
            job.label = label;
            job.type = type;
            job.status = status;

            return job;
        }

    }
    
}
