package cz.matfyz.tests.example.common;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.core.utils.Config;

import org.checkerframework.checker.nullness.qual.Nullable;

public class SparkProvider {

    private static final Config config = new Config("tests.spark");

    private @Nullable SparkSettings settings;

    public SparkSettings getSettings() {
        if (settings == null) {
            settings = new SparkSettings(
                config.get("master"),
                config.get("checkpointDir")
            );
        }

        return settings;
    }

}
