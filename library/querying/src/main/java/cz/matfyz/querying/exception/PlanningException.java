package cz.matfyz.querying.exception;

import java.io.Serializable;

public class PlanningException extends QueryingException {

    protected PlanningException(String name, Serializable data, Throwable cause) {
        super("planning." + name, data, cause);
    }

    public static PlanningException noPlans() {
        return new PlanningException("noPlans", null, null);
    }

}
