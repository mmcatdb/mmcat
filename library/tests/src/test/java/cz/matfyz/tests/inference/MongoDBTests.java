package cz.matfyz.tests.inference;

import cz.matfyz.tests.example.common.SparkProvider;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrappermongodb.MongoDBInferenceWrapper;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrappermongodb.MongoDBProvider.MongoDBSettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MongoDBTests {

    private final SparkProvider sparkProvider = new SparkProvider();

    MongoDBInferenceWrapper setup(MongoDBProvider mongoDBProvider, String kindName) {
        final var wrapper = new MongoDBControlWrapper(mongoDBProvider).getInferenceWrapper(sparkProvider.getSettings())
            .copyForKind(kindName);
        wrapper.startSession();

        return (MongoDBInferenceWrapper) wrapper;
    }

    @Test
    void testLoadDocumentsBasic() throws Exception {
        // TODO: make sure that this db actually exists -> will need to set it up
        final var settings = new MongoDBSettings(
            "localhost",
            "3205",
            "admin",
            "yelpbusiness",
            "user",
            "password",
            false,
            false
        );
        final var mongoDBProvider = new MongoDBProvider(settings);
        MongoDBInferenceWrapper inferenceWrapper = setup(mongoDBProvider, "yelpbusinesssample");

        var records = inferenceWrapper.loadRecords();

        assertNotNull(records, "Records should not be null");
        assertFalse(records.isEmpty(), "Records should not be empty");
        assertEquals(2, records.count(), "There should be two records");
    }
}
