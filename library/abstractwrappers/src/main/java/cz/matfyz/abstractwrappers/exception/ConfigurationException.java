package cz.matfyz.abstractwrappers.exception;

public class ConfigurationException extends WrapperException {

    private ConfigurationException(String name) {
        super("configuration." + name, null, null);
    }

    public static ConfigurationException missingProvider() {
        return new ConfigurationException("missingProvider");
    }

    public static ConfigurationException missingSparkSettings() {
        return new ConfigurationException("missingSparkSettings");
    }

}
