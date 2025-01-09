package cz.matfyz.core.record;

public record AdminerFilter(
    String columnName,
    String operator,
    String columnValue
) {}
