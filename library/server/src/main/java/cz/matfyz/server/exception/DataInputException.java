package cz.matfyz.server.exception;

import cz.matfyz.abstractwrappers.datainput.DataInput.DataInputType;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datainput.DataInputEntity;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class DataInputException extends ServerException {

    private record Data(
        Id dataInputId,
        DataInputType type
    ) implements Serializable {}

    private DataInputException(String name, DataInputEntity dataInput, Throwable cause) {
        super("dataInput." + name, new Data(dataInput.id, dataInput.type), cause);
    }

    public static DataInputException wrapperNotFound(DataInputEntity dataInput) {
        return new DataInputException("wrapperNotFound", dataInput, null);
    }

    public static DataInputException wrapperNotCreated(DataInputEntity dataInput, Throwable cause) {
        return new DataInputException("wrapperNotCreated", dataInput, cause);
    }

}
