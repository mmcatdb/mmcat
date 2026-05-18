package cz.matfyz.querying.core;

import cz.matfyz.core.querying.ListResult;
import cz.matfyz.querying.planner.QueryPlan;

public record QueryExecution(
    QueryPlan plan,
    ListResult result,
    /** Time of parsing and planning. */
    double planningTimeInMs,
    /** Time of evaluating the query plan in our system (selection and projection). */
    double selectionTimeInMs,
    double underlyingDBMSSelectionTimeInMs,
    double projectionTimeInMs
) {}
