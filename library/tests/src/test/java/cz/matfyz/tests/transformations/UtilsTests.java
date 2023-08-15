package cz.matfyz.tests.transformations;

import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

/**
 * @author jachymb.bartik
 */
public class UtilsTests {
   
    @Test
    public void postgreSQLSplitScript() {
        final var script = """
        a b;
        c d;

        e f ;

        g h
        i j;

        a b; c d;

        -- comment1 x y
        e f; -- comment2
        -- comment3
        g h;
        """;

        final var expected = new String[] {
            "a b",
            "c d",
            "e f",
            "g h\ni j",
            "a b",
            "c d",
            // "e f",
            // "g h"
        };

        final var actual = PostgreSQLControlWrapper.splitScript(script).toArray(String[]::new);
        assertArrayEquals(expected, actual);
    }

}
