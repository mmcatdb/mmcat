package cz.matfyz.tests.inference;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.tests.example.common.SparkProvider;
import cz.matfyz.wrapperjson.JsonControlWrapper;
import cz.matfyz.wrapperjson.JsonProvider;
import cz.matfyz.wrapperjson.JsonProvider.JsonSettings;
import cz.matfyz.wrappercsv.CsvControlWrapper;
import cz.matfyz.wrappercsv.CsvProvider;
import cz.matfyz.wrappercsv.CsvProvider.CsvSettings;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class RSDTests {

    private final SparkProvider sparkProvider = new SparkProvider();

    @Test
    void testRBAalgorithmJSON() throws Exception {
        final var url = ClassLoader.getSystemResource("RSDTestFile.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false, false);
        final var provider = new JsonProvider(settings);

        final AbstractInferenceWrapper inference = new JsonControlWrapper(provider)
            .enableSpark(sparkProvider.getSettings())
            .getInferenceWrapper(provider.getKindName());

        final Method privateExecuteRBA = MMInferOneInAll.class.getDeclaredMethod("executeRBA", AbstractInferenceWrapper.class);
        privateExecuteRBA.setAccessible(true);

        final MMInferOneInAll mmInferOneInAll = new MMInferOneInAll();
        final var rsd = (RecordSchemaDescription) privateExecuteRBA.invoke(mmInferOneInAll, inference);
    }

    @Test
    void testRBAalgorithmCSV() throws Exception {
        final var url = ClassLoader.getSystemResource("RSDTestFile.csv");
        final var settings = new CsvSettings(url.toURI().toString(), ',', true, false, false, false);
        final var provider = new CsvProvider(settings);

        final AbstractInferenceWrapper inference = new CsvControlWrapper(provider)
            .enableSpark(sparkProvider.getSettings())
            .getInferenceWrapper(provider.getKindName());

        final Method privateExecuteRBA = MMInferOneInAll.class.getDeclaredMethod("executeRBA", AbstractInferenceWrapper.class);
        privateExecuteRBA.setAccessible(true);

        final MMInferOneInAll mmInferOneInAll = new MMInferOneInAll();
        final var rsd = (RecordSchemaDescription) privateExecuteRBA.invoke(mmInferOneInAll, inference);
    }

}
