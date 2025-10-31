package cz.matfyz.tests.mock;

import cz.matfyz.wrappercsv.CsvProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MockCsvProvider extends CsvProvider {

    private final String content;

    public MockCsvProvider(char separator, boolean hasHeader, String content) {
        super(new CsvSettings("mock://csv", separator, hasHeader, false, false, false));
        this.content = content;
    }

    @Override public InputStream getInputStream() {
        return new ByteArrayInputStream(content.getBytes());
    }

}
