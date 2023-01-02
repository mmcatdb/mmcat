package cz.cuni.matfyz.server.entity.datasource;

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
public class DataSource extends Entity implements JSONConvertible {

    public String url;
    public String label;
    public Type type;

    private DataSource(Id id) {
        super(id);
    }

    public enum Type {
        JsonLdStore
    }

    public void updateFrom(DataSourceUpdate update) {
        if (update.url() != null)
            this.url = update.url();
        
        if (update.label() != null)
            this.label = update.label();
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<DataSource> {

        @Override
        protected JSONObject innerToJSON(DataSource object) throws JSONException {
            var output = new JSONObject();

            output.put("url", object.url);
            output.put("label", object.label);
            output.put("type", object.type);

            return output;
        }

    }

    public static class Builder extends FromJSONLoaderBase<DataSource> {

        public DataSource fromJSON(Id id, String jsonValue) {
            var dataSource = new DataSource(id);
            loadFromJSON(dataSource, jsonValue);
            return dataSource;
        }

        @Override
        protected void innerLoadFromJSON(DataSource dataSource, JSONObject jsonObject) throws JSONException {
            dataSource.url = jsonObject.getString("url");
            dataSource.label = jsonObject.getString("label");
            dataSource.type = Type.valueOf(jsonObject.getString("type"));
        }

        public DataSource fromDataSource(Id id, DataSource input) {
            final var dataSource = new DataSource(id);
            dataSource.url = input.url;
            dataSource.label = input.label;
            dataSource.type = input.type;

            return dataSource;
        }

        public DataSource fromInit(DataSourceInit init) {
            final var dataSource = new DataSource(null);
            dataSource.url = init.url();
            dataSource.label = init.label();
            dataSource.type = init.type();

            return dataSource;
        }

    }
    
}
