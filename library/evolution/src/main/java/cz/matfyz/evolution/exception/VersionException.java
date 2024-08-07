package cz.matfyz.evolution.exception;

public class VersionException extends EvolutionException {

    protected VersionException(String type, String value) {
        super("version", value, null);
    }

    public static VersionException parse(String  value) {
        return new VersionException("parse", value);
    }

}
