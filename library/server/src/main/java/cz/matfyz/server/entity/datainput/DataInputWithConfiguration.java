package cz.matfyz.server.entity.datainput;

import cz.matfyz.abstractwrappers.datainput.DataInput.DataInputType;
import cz.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public class DataInputWithConfiguration {

    public final Id id;
    public final DataInputType type;
    public final String label;
    public final DataInputConfiguration configuration;

    public DataInputWithConfiguration(DataInputEntity dataInput, DataInputConfiguration configuration) {
        this.id = dataInput.id;
        this.type = dataInput.type;
        this.label = dataInput.label;
        this.configuration = configuration;
    }

}

