package cz.matfyz.tests.mock;

import cz.matfyz.wrapperjson.JsonProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MockJsonProvider extends JsonProvider {

    private final String content;

    public MockJsonProvider(String content) {
        super(new JsonSettings("mock://json", false, false, false));
        this.content = content;
    }

    @Override public InputStream getInputStream() {
        return new ByteArrayInputStream(content.getBytes());
    }

}
