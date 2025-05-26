package cz.matfyz.inference.schemaconversion.utils;

import cz.matfyz.core.rsd.Candidates;

import java.util.List;

/**
 * A record representing the result of an inference operation.
 */
public record InferenceResult(
    List<CategoryMappingsPair> pairs,
    Candidates candidates
) {}
