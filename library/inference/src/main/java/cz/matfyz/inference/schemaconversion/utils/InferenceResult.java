package cz.matfyz.inference.schemaconversion.utils;

import cz.matfyz.core.rsd.Candidates;

import java.util.List;

/**
 * A record representing the result of an inference operation, consisting of a list of
 * {@link CategoryMappingPair} instances and a {@link Candidates} object.
 *
 * @param pairs The list of {@link CategoryMappingPair} instances representing the schema and metadata results of the inference.
 * @param candidates The {@link Candidates} object containing potential primary key and reference candidates inferred from the data.
 */
public record InferenceResult(
    List<CategoryMappingPair> pairs,
    Candidates candidates
) {}
