package cz.matfyz.inference.adminer;

import java.util.List;
import java.util.Map;

public interface AdminerAlgorithmsInterface {
    /**
     * Returns a mapping of operator names to their corresponding database-specific representations.
     *
     * @return A map where keys are logical operator names and values are their respective database-specific representations.
     */
    Map<String, String> getOperators();

    /**
     * Returns a list of unary operators.
     *
     * @return A list of unary operator names supported by the database.
     */
    List<String> getUnaryOperators();
}
