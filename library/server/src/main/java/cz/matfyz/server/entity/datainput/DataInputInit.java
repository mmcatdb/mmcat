package cz.matfyz.server.entity.datainput;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.matfyz.abstractwrappers.datainput.DataInput.DataInputType;
import cz.matfyz.server.entity.datainput.DataInputUpdate;


public class DataInputInit extends DataInputUpdate {

    public final DataInputType type;

    @JsonCreator
    public DataInputInit(
        @JsonProperty("label") String label,
        @JsonProperty("type") DataInputType type,
        @JsonProperty("settings") ObjectNode settings
    ) {
        super(label, settings);
        this.type = type;
    }

}