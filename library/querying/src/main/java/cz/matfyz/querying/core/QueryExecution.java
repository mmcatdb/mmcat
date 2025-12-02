package cz.matfyz.querying.core;

import cz.matfyz.core.querying.ListResult;

public record QueryExecution(
    ListResult result,
    /** Time of parsing and planning. */
    long planningTimeInMs,
    /** Time of evaluating the query plan in our system (selection and projection). */
    long evaluationTimeInMs
) {}
