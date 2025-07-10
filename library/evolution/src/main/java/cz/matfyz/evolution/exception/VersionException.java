package cz.matfyz.evolution.exception;

import cz.matfyz.evolution.Version;

import java.io.Serializable;

public class VersionException extends EvolutionException {

    protected VersionException(String name, Serializable value) {
        super("version." + name, value, null);
    }

    public static VersionException parse(String value) {
        return new VersionException("parse", value);
    }

    private record MismatchData(
        Version expected,
        Version actual
    ) implements Serializable {}

    public static VersionException mismatch(Version expected, Version actual) {
        return new VersionException("mismatch", new MismatchData(expected, actual));
    }

}
