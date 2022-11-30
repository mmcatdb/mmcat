package cz.cuni.matfyz.server.entity;

import cz.cuni.matfyz.core.serialization.FromJSONLoaderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jachym.bartik
 */
public class Job extends Entity implements JSONConvertible {

    public final int mappingId;
    public final Integer categoryId;
    public String label;
    public Type type;
    public Status status;

    /*
    public Job(
        @JsonProperty("id") Integer id,
        @JsonProperty("mappingId") int mappingId,
        @JsonProperty("status") Status status
    ) {
        super(id);
        this.mappingId = mappingId;
        this.status = status;
    }
    */

    private Job(Integer id, int mappingId, Integer categoryId) {
        super(id);
        this.mappingId = mappingId;
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

        public Job fromJSON(int id, int mappingId, int categoryId, String jsonValue) {
            var job = new Job(id, mappingId, categoryId);
            loadFromJSON(job, jsonValue);
            return job;
        }

        @Override
        protected void innerLoadFromJSON(Job job, JSONObject jsonObject) throws JSONException {
            job.label = jsonObject.getString("label");
            job.type = Type.valueOf(jsonObject.getString("type"));
            job.status = Status.valueOf(jsonObject.getString("status"));
        }

        public Job fromArguments(Integer id, int mappingId, Integer categoryId, String label, Type type, Status status) {
            var job = new Job(id, mappingId, categoryId);
            job.label = label;
            job.type = type;
            job.status = status;

            return job;
        }

    }
    
}
