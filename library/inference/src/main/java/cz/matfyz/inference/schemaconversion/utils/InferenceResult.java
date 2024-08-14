package cz.matfyz.inference.schemaconversion.utils;

import cz.matfyz.core.rsd.Candidates;

import java.util.List;

public record InferenceResult(
    List<CategoryMappingPair> pairs,
    Candidates candidates
) {}
