package cz.matfyz.querying.core;

import cz.matfyz.core.querying.ListResult;
import cz.matfyz.querying.planner.QueryPlan;

public record QueryExecution(
    QueryPlan plan,
    ListResult result,
    /** Time of parsing and planning. */
    long planningTimeInMs,
    /** Time of evaluating the query plan in our system (selection and projection). */
    long selectionTimeInMs,
    long underlyingDBMSSelectionTimeInMs,
    long projectionTimeInMs
) {}
