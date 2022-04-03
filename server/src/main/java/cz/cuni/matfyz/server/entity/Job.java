package cz.cuni.matfyz.server.entity;

import org.json.JSONException;
import org.json.JSONObject;

import cz.cuni.matfyz.core.serialization.FromJSONLoaderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

/**
 * 
 * @author jachym.bartik
 */
public class Job extends Entity implements JSONConvertible {

    public final int mappingId;
    public Status status;
    public String name;

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

    private Job(Integer id, int mappingId) {
        super(id);
        this.mappingId = mappingId;
    }

    public enum Status {
        Default, // The job isn't created yet.
        Ready, // The job can be started now.
        Running, // The job is currently being processed.
        Finished, // The job is finished, either with a success or with an error.
        Canceled // The job was canceled while being in one of the previous states. It can never be started (again).
    }

    @Override public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<Job> {

		@Override
        protected JSONObject _toJSON(Job object) throws JSONException {
            var output = new JSONObject();

			output.put("status", object.status.toString());
            output.put("name", object.name.toString());

            return output;
        }

	}

	public static class Builder extends FromJSONLoaderBase<Job> {

		public Job fromJSON(int id, int mappingId, String jsonValue) {
			var job = new Job(id, mappingId);
			loadFromJSON(job, jsonValue);
			return job;
		}

        @Override
        protected void _loadFromJSON(Job job, JSONObject jsonObject) throws JSONException {
            job.status = Status.valueOf(jsonObject.getString("status"));
            job.name = jsonObject.getString("name");
        }

        public Job fromArguments(Integer id, int mappingId, Status status, String name) {
            var job = new Job(id, mappingId);
            job.status = status;
            job.name = name;
            return job;
        }

    }
    
}
