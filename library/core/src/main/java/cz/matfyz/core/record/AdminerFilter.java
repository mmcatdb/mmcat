package cz.matfyz.core.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// FIXME Proč je toto v package record?

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminerFilter(
    String propertyName,
    String operator,
    String propertyValue
) {}
