package cz.matfyz.core.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminerFilter(
    String propertyName,
    String operator,
    String propertyValue
) {}
