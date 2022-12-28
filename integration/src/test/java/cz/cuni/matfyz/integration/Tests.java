package cz.cuni.matfyz.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

public class Tests {
    static final String JSON_LD_FILE_NAME = "test2.jsonld";

    @Test
    public void jsonLdToRDF_DoesNotThrow() {
        final var transformation = new JsonLdToRDF();
        transformation.input(JSON_LD_FILE_NAME);

        assertDoesNotThrow(() -> {
            transformation.algorithm();
        });
    }
}
