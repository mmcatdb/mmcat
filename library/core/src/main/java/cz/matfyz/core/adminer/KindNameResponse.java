package cz.matfyz.core.adminer;

import java.util.List;

/**
 * Represents a response with kind names.
 */
// FIXME Pokud to je více jmen, mělo by to být v množném čísle.
public record KindNameResponse(List<String> data) {}
