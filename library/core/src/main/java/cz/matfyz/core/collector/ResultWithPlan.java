package cz.matfyz.core.collector;

/** A small auxiliary class to pass multiple values out of a function. */
public record ResultWithPlan<TResult, TPlan>(TResult result, TPlan plan) {}
